package lu.r3flexi0n.bungeeonlinetime.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier
import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.config.ConfigLoader
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.db.MySQLDatabase
import lu.r3flexi0n.bungeeonlinetime.common.db.SQLiteDatabase
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Utils
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit

@Plugin(id = "onlinetime", name = "OnlineTime")
class VelocityOnlineTimePlugin @Inject constructor(
    private val proxy: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataFolder: Path
) {

    lateinit var config: Config

    lateinit var database: Database

    val onlineTimePlayers = HashMap<UUID, OnlineTimePlayer>()

    val pluginMessageChannel = LegacyChannelIdentifier("bungeeonlinetime:get")


    @Subscribe
    fun onEnable(e: ProxyInitializeEvent) {
        try {
            config = ConfigLoader.load(dataFolder, logger)
        } catch (ex: IOException) {
            logger.error("Error while creating or loading. Disabling plugin...", ex)
            return
        }

        database = if (config.mySQL.enabled) {
            MySQLDatabase(config.mySQL)
        } else {
            val databaseFile = File(dataFolder.toFile(), "BungeeOnlineTime.db")
            SQLiteDatabase(databaseFile)
        }

        try {
            logger.info("Connecting to ${database.dbName}...")
            database.openConnection()
            database.createTable()
            database.createIndex()
            logger.info("Successfully connected to ${database.dbName}")
        } catch (ex: Exception) {
            logger.error("Error while connecting to ${database.dbName}. Disabling plugin...")
            ex.printStackTrace()
            return
        }

        val commandAliases = config.plugin.commandAliases
        val command = OnlineTimeCommand(this)

        proxy.commandManager.register(commandAliases[0], command, *commandAliases)
        proxy.eventManager.register(this, OnlineTimeListener(this))

        if (config.plugin.usePlaceholderApi) {
            proxy.channelRegistrar.register(pluginMessageChannel)
            val timerInterval = config.plugin.placeholderRefreshTimer
            if (timerInterval > 0) {
                proxy.scheduler.buildTask(this) { ->
                    for (player in proxy.allPlayers) {
                        val onlineTimePlayer = onlineTimePlayers[player.uniqueId] ?: continue
                        val arr = Utils.onlineTimePluginMessageArr(onlineTimePlayer, player.uniqueId)
                        if (arr != null)
                            player.currentServer.ifPresent {it.sendPluginMessage(pluginMessageChannel, arr) }
                    }
                }.repeat(timerInterval.toLong(), TimeUnit.MINUTES).schedule()
            }
        }
    }
}