package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.config.ConfigLoader
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.db.MySQLDatabase
import lu.r3flexi0n.bungeeonlinetime.common.db.SQLiteDatabase
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Utils
import net.md_5.bungee.api.plugin.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

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
            SQLiteDatabase(File(dataFolder, "BungeeOnlineTime.db"))
        }

        try {
            logger.info("Connecting to ${database.dbName}...")
            database.openConnection()
            database.createTable()
            database.createIndex()
            logger.info("Successfully connected to ${database.dbName}")
        } catch (ex: Exception) {
            logger.error("Error while connecting to ${database.dbName}. Disabling plugin...", ex)
            return
        }

        val commandAliases = config.plugin.commandAliases
        val command = OnlineTimeCommand(this, commandAliases[0], commandAliases)

        proxy.pluginManager.registerCommand(this, command)
        proxy.pluginManager.registerListener(this, OnlineTimeListener(this))

        if (config.plugin.usePlaceholderApi) {
            proxy.registerChannel(pluginMessageChannel)
            val timerInterval = config.plugin.placeholderRefreshTimer
            if (timerInterval > 0) {
                proxy.scheduler.schedule(this, {
                    for (player in proxy.players) {
                        val onlineTimePlayer = onlineTimePlayers[player.uniqueId] ?: continue
                        val arr = Utils.createPluginMessageArr(onlineTimePlayer, player.uniqueId)
                        if (arr != null)
                            player.server?.sendData(pluginMessageChannel, arr)
                    }
                }, 0L, timerInterval.toLong(), TimeUnit.MINUTES)
            }
        }
    }
}