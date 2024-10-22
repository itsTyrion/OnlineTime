package lu.r3flexi0n.bungeeonlinetime.common.db

import java.nio.file.Path

class SQLiteDatabase(path: Path) : Database("SQLite", arrayOf("org.sqlite.JDBC"), "jdbc:sqlite:$path") {

    override fun createIndex() {
        con.createStatement().use { st ->
            st.executeUpdate("CREATE INDEX IF NOT EXISTS BungeeOnlineTimeIndex ON BungeeOnlineTime (name, time);")
        }
    }

    override val insertQuery = "INSERT OR IGNORE INTO BungeeOnlineTime VALUES (?, ?, ?);"
}