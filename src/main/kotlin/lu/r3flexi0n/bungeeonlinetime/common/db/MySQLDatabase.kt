package lu.r3flexi0n.bungeeonlinetime.common.db

import lu.r3flexi0n.bungeeonlinetime.common.config.Config

class MySQLDatabase(config: Config.MySQL) : Database(
    "MySQL",
    arrayOf("com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"),
    "jdbc:mysql://${config.host}:${config.port}/${config.database}"
) {
    init {
        dbProperties["user"] = config.username
        dbProperties["password"] = config.password
        dbProperties["autoReconnect"] = "true"
    }

    override fun createIndex() {
        //CREATE INDEX IF NOT EXISTS is not available in mysql...
        val sql = "SHOW INDEX FROM BungeeOnlineTime WHERE Key_Name = 'BungeeOnlineTimeIndex';"
        con.createStatement().use { st ->
            st.executeQuery(sql).use { result ->
                if (!result.next()) { // ... so we need to manually check if it exists
                    con.createStatement().use { st1 ->
                        st1.executeUpdate("CREATE INDEX BungeeOnlineTimeIndex ON BungeeOnlineTime (name, time);")
                    }
                }
            }
        }
    }

    override val insertQuery = "INSERT IGNORE INTO BungeeOnlineTime VALUES (?, ?, ?);"
}