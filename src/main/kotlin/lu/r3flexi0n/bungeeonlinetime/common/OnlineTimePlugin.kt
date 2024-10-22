package lu.r3flexi0n.bungeeonlinetime.common

import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.config.ConfigLoader
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.db.MySQLDatabase
import lu.r3flexi0n.bungeeonlinetime.common.db.SQLiteDatabase
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Path
import java.util.UUID

interface OnlineTimePlugin {
    var database: Database
    val onlineTimePlayers: HashMap<UUID, OnlineTimePlayer>
    val logger: Logger
    var config: Config
    val dataPath: Path

    fun reloadConfig() {
        try {
            config = ConfigLoader.load(dataPath, logger)
        } catch (ex: IOException) {
            logger.error("Error while loading config.", ex)
            return
        }
        database.close()

        connectDB()
    }

    fun connectDB() {
        database = if (config.mySQL.enabled) {
            MySQLDatabase(config.mySQL)
        } else {
            SQLiteDatabase(dataPath.resolve("BungeeOnlineTime.db"))
        }
        logger.info("Connecting to ${database.dbName}...")
        database.openConnection()
        database.createTable()
        database.createIndex()
        logger.info("Successfully connected to ${database.dbName}")
    }
}
