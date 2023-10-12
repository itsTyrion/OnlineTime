package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.config.ConfigLoader
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.db.MySQLDatabase
import lu.r3flexi0n.bungeeonlinetime.common.db.SQLiteDatabase
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import net.md_5.bungee.api.plugin.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*

class BungeeOnlineTimePlugin : Plugin() {

    lateinit var config: Config

    lateinit var database: Database

    val onlineTimePlayers = HashMap<UUID, OnlineTimePlayer>()

    val pluginMessageChannel = "bungeeonlinetime:get"

    val logger: Logger = LoggerFactory.getLogger(super.getLogger().name)

    override fun onEnable() {
        try {
            config = ConfigLoader.load(dataFolder.toPath(), logger)
        } catch (ex: IOException) {
            logger.error("Error while creating or loading. Disabling plugin...", ex)
            return
        }

        database = if (config.mySQL.enabled) {
            MySQLDatabase(config.mySQL)
        } else {
            val databaseFile = File(dataFolder, "BungeeOnlineTime.db")
            SQLiteDatabase(databaseFile)
        }

        try {
            logger.info("Connecting to " + database.dbName + "...")
            database.openConnection()
            database.createTable()
            database.createIndex()
            logger.info("Successfully connected to " + database.dbName + ".")
        } catch (ex: Exception) {
            logger.error("Error while connecting to " + database.dbName + ". Disabling plugin...", ex)
            return
        }

        val commandAliases = config.plugin.commandAliases
        val command = OnlineTimeCommand(this, commandAliases[0], commandAliases)

        proxy.pluginManager.registerCommand(this, command)
        proxy.pluginManager.registerListener(this, OnlineTimeListener(this))

        if (config.plugin.usePlaceholderApi) {
            proxy.registerChannel(pluginMessageChannel)
        }
    }
}