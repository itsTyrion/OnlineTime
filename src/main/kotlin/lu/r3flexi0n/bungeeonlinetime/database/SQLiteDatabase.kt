package lu.r3flexi0n.bungeeonlinetime.database

import java.io.File

class SQLiteDatabase(file: File) : Database(
    "SQLite",
    arrayOf("org.sqlite.JDBC"),
    "jdbc:sqlite:" + file.path
)