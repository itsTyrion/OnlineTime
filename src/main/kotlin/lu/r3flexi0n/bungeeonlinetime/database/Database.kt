package lu.r3flexi0n.bungeeonlinetime.database

import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTime
import java.sql.*
import java.util.*

abstract class Database(val databaseName: String, private val databaseClass: Array<String>, private val databaseUrl: String) {
    var databaseProperties = Properties()
    @Suppress("LeakingThis")
    private val mysql = this is MySQLDatabase

    private fun isSupported(): Boolean {
        databaseClass.forEach { name ->
            try {
                Class.forName(name)
                return true
            } catch (_: ClassNotFoundException) {}
        }
        return false
    }

    private var con: Connection? = null

    @Throws(SQLException::class)
    fun openConnection() {
        if (con == null) {
            val supported = isSupported() //todo
            con = DriverManager.getConnection(databaseUrl, databaseProperties)
        }
    }

    @Throws(SQLException::class)
    fun createTable() {
        val sql = "CREATE TABLE IF NOT EXISTS BungeeOnlineTime (uuid VARCHAR(36) UNIQUE, name VARCHAR(16), time BIGINT);"
        con!!.createStatement().use { st ->
            st.executeUpdate(sql)
        }
    }

    @Throws(SQLException::class)
    fun createIndex() {
        if (mysql) {
            //CREATE INDEX IF NOT EXISTS is not available in mysql...
            val sql = "SHOW INDEX FROM BungeeOnlineTime WHERE Key_Name = 'BungeeOnlineTimeIndex';"
            con!!.createStatement().use { st ->
                st.executeQuery(sql).use { result ->
                    if (!result.next()) { // ... so we need to manually check if it exists
                        con!!.createStatement().use { st1 ->
                            st1.executeUpdate("CREATE INDEX BungeeOnlineTimeIndex ON BungeeOnlineTime (name, time);")
                        }
                    }
                }
            }
        } else {
            con!!.createStatement().use { st ->
                st.executeUpdate("CREATE INDEX IF NOT EXISTS BungeeOnlineTimeIndex ON BungeeOnlineTime (name, time);")
            }
        }
    }

    @Throws(SQLException::class)
    fun updateOnlineTime(uuid: String, name: String, time: Long) {
        val sql = if (mysql) {
            "INSERT IGNORE INTO BungeeOnlineTime VALUES (?, ?, ?);"
        } else
            "INSERT OR IGNORE INTO BungeeOnlineTime VALUES (?, ?, ?);"

        con!!.prepareStatement(sql).use { st ->
            st.setString(1, uuid)
            st.setString(2, name)
            st.setLong(3, 0L)
            st.executeUpdate()
        }

        con!!.prepareStatement("UPDATE BungeeOnlineTime SET name = ?, time = time + ? WHERE uuid = ?;").use { st ->
            st.setString(1, name)
            st.setLong(2, time)
            st.setString(3, uuid)
            st.executeUpdate()
        }
    }

    @Throws(SQLException::class)
    fun getOnlineTime(uuidOrName: String): List<OnlineTime> {
        val sql = if (uuidOrName.length == 36) {
            "SELECT * FROM BungeeOnlineTime WHERE uuid = ?;"
        } else
            "SELECT * FROM BungeeOnlineTime WHERE name = ?;"

        return con!!.prepareStatement(sql).use { st ->
            st.setString(1, uuidOrName)
            st.executeQuery().use(::getOnlineTimesFromResultSet)
        }
    }

    @Throws(SQLException::class)
    fun getTopOnlineTimes(page: Int, perPage: Int): List<OnlineTime> {
        val sql = "SELECT * FROM BungeeOnlineTime ORDER BY time DESC LIMIT ? OFFSET ?;"

        return con!!.prepareStatement(sql).use { st ->
            st.setInt(1, perPage)
            st.setInt(2, (page - 1) * perPage)
            st.executeQuery().use(::getOnlineTimesFromResultSet)
        }
    }

    @Throws(SQLException::class)
    fun resetOnlineTime(name: String) {
        val sql = "DELETE FROM BungeeOnlineTime WHERE name = ?;"
        con!!.prepareStatement(sql).use { st ->
            st.setString(1, name)
            st.executeUpdate()
        }
    }

    @Throws(SQLException::class)
    fun resetAllOnlineTimes() {
        val sql = "DELETE FROM BungeeOnlineTime;"
        con!!.createStatement().use { st -> st.executeUpdate(sql) }
    }

    @Throws(SQLException::class)
    private fun getOnlineTimesFromResultSet(resultSet: ResultSet): List<OnlineTime> {
        val result: MutableList<OnlineTime> = ArrayList()
        while (resultSet.next()) {
            val uuid = UUID.fromString(resultSet.getString("uuid"))
            val name = resultSet.getString("name")
            val time = resultSet.getLong("time")
            result.add(OnlineTime(uuid, name, time))
        }
        return result
    } /*

    public void generateFakeDatabase() throws SQLException {
        connection.setAutoCommit(false);

        Random random = new Random();
        for (int i = 0; i < 5000; i++) {
            UUID uuid = UUID.randomUUID();
            String name = getRandomString(8);
            long time = random.nextInt(360000000);
            updateOnlineTime(uuid.toString(), name, time);
        }

        connection.commit();
        connection.setAutoCommit(true);
    }

    private String getRandomString(int length) {
        Random random = new Random();
        char[] characters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] randomString = new char[length];
        for (int i = 0; i < length; i++) {
            randomString[i] = characters[random.nextInt(characters.length)];
        }
        return String.valueOf(randomString);
    }

    */
}