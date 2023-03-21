package lu.r3flexi0n.bungeeonlinetime.database

class MySQLDatabase(host: String, port: Int, database: String, username: String, password: String) : Database(
    "MySQL",
    arrayOf("com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"),
    "jdbc:mysql://$host:$port/$database"
) {
    init {
        databaseProperties["user"] = username
        databaseProperties["password"] = password
        databaseProperties["autoReconnect"] = "true"
    }
}