package lu.r3flexi0n.bungeeonlinetime.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier
import de.itsTyrion.pluginAnnotation.velocity.VelocityPlugin
import lu.r3flexi0n.bungeeonlinetime.common.OnlineTimePlugin
import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.config.ConfigLoader
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Messaging
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit

@VelocityPlugin(id = "onlinetime", name = "OnlineTime", authors = ["itsTyrion", "r3flexi0n"])
class VelocityOnlineTimePlugin @Inject constructor(
    val proxy: ProxyServer,
    override val logger: Logger,
    @DataDirectory override val dataPath: Path
) : OnlineTimePlugin {

    override lateinit var config: Config
    override lateinit var database: Database

    override val onlineTimePlayers = HashMap<UUID, OnlineTimePlayer>()

    val pluginMessageChannelMain = LegacyChannelIdentifier(Messaging.CHANNEL_MAIN)
    val pluginMessageChannelTop = LegacyChannelIdentifier(Messaging.CHANNEL_TOP)


    @Subscribe
    fun onEnable(@Suppress("unused_parameter") e: ProxyInitializeEvent) {
        try {
            config = ConfigLoader.load(dataPath, logger)
        } catch (ex: IOException) {
            logger.error("Error while creating or loading. Disabling plugin...", ex)
            return
        }

        try {
            connectDB()
        } catch (ex: Exception) {
            logger.error("Error while connecting to ${database.dbName}. Disabling plugin...")
            ex.printStackTrace()
            return
        }

        val commandAliases = config.plugin.commandAliases
        val command = OnlineTimeCommand(this)

        val cmdMgr = proxy.commandManager
        cmdMgr.register(cmdMgr.metaBuilder(commandAliases[0]).aliases(*commandAliases).plugin(this).build(), command)
        proxy.eventManager.register(this, OnlineTimeListener(this))

        if (config.plugin.usePlaceholderApi) {
            proxy.channelRegistrar.register(pluginMessageChannelMain, pluginMessageChannelTop)
            val timerInterval = config.plugin.placeholderRefreshSeconds
            if (timerInterval > 0) {
                proxy.scheduler.buildTask(this) { ->
                    for (player in proxy.allPlayers) {
                        val onlineTimePlayer = onlineTimePlayers[player.uniqueId] ?: continue
                        val arr = Messaging.createMainArr(onlineTimePlayer, player.uniqueId)
                        if (arr != null)
                            player.currentServer.ifPresent { it.sendPluginMessage(pluginMessageChannelMain, arr) }
                    }

                    val arr = Messaging.createTopArr(this)
                    for (server in proxy.allServers) {
                        server.sendPluginMessage(pluginMessageChannelTop, arr)
                    }

                }.repeat(timerInterval.toLong(), TimeUnit.SECONDS).schedule()
            }
        }
    }
}