package lu.r3flexi0n.bungeeonlinetime.common.db

import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTime
import java.sql.*
import java.util.*

@Suppress("SqlNoDataSourceInspection")
abstract class Database(val dbName: String, private val dbClass: Array<String>, private val dbURL: String) {
    var dbProperties = Properties(3)

    private fun isSupported(): Boolean {
        dbClass.forEach { name ->
            try {
                Class.forName(name)
                return true
            } catch (_: ClassNotFoundException) {
            }
        }
        return false
    }

    protected lateinit var con: Connection

    @Throws(SQLException::class)
    fun openConnection() {
        if (::con.isInitialized.not()) {
            check(isSupported()) { "No supported database driver present!" }
            con = DriverManager.getConnection(dbURL, dbProperties)
        }
    }

    @Throws(SQLException::class)
    fun createTable() {
        val sql =
            "CREATE TABLE IF NOT EXISTS BungeeOnlineTime (uuid VARCHAR(36) UNIQUE, name VARCHAR(16), time BIGINT);"
        con.createStatement().use { it.executeUpdate(sql) }
    }

    fun close() {
        if (::con.isInitialized)
            con.close()
    }

    @Throws(SQLException::class)
    abstract fun createIndex()

    abstract val insertQuery: String

    @Throws(SQLException::class)
    fun updateOnlineTime(uuid: String, name: String, time: Long) {
        con.prepareStatement(insertQuery).use { st ->
            st.setString(1, uuid)
            st.setString(2, name)
            st.setLong(3, 0L)
            st.executeUpdate()
        }

        con.prepareStatement("UPDATE BungeeOnlineTime SET name = ?, time = time + ? WHERE uuid = ?;").use { st ->
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

        return con.prepareStatement(sql).use { st ->
            st.setString(1, uuidOrName)
            st.executeQuery().use(::getOnlineTimesFromResultSet)
        }
    }

    @Throws(SQLException::class)
    fun getTopOnlineTimes(page: Int, perPage: Int): List<OnlineTime> {
        val sql = "SELECT * FROM BungeeOnlineTime ORDER BY time DESC LIMIT ? OFFSET ?;"

        return con.prepareStatement(sql).use { st ->
            st.setInt(1, perPage)
            st.setInt(2, (page - 1) * perPage)
            st.executeQuery().use(::getOnlineTimesFromResultSet)
        }
    }

    @Throws(SQLException::class)
    fun resetOnlineTime(name: String) {
        val sql = "DELETE FROM BungeeOnlineTime WHERE name = ?;"
        con.prepareStatement(sql).use { st ->
            st.setString(1, name)
            st.executeUpdate()
        }
    }

    @Throws(SQLException::class)
    fun resetAllOnlineTimes() {
        val sql = "DELETE FROM BungeeOnlineTime;"
        con.createStatement().use { it.executeUpdate(sql) }
    }

    @Throws(SQLException::class)
    private fun getOnlineTimesFromResultSet(resultSet: ResultSet): List<OnlineTime> {
        val result = ArrayList<OnlineTime>()
        while (resultSet.next()) {
            val uuid = UUID.fromString(resultSet.getString("uuid"))
            val name = resultSet.getString("name")
            val time = resultSet.getLong("time")
            result.add(OnlineTime(uuid, name, time))
        }
        return result
    }
}