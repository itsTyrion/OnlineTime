package lu.r3flexi0n.bungeeonlinetime.bungee

import de.itsTyrion.pluginAnnotation.bungee.BungeePlugin
import lu.r3flexi0n.bungeeonlinetime.common.OnlineTimePlugin
import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.config.ConfigLoader
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Messaging
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit

@BungeePlugin(name = "BungeeOnlineTime", author = "itsTyrion, R3fleXi0n")
class BungeeOnlineTimePlugin : Plugin(), OnlineTimePlugin {

    override lateinit var config: Config
    override lateinit var database: Database
    override val dataPath: Path = dataFolder.toPath()

    override val onlineTimePlayers = HashMap<UUID, OnlineTimePlayer>()

    override val logger: Logger = LoggerFactory.getLogger(super.getLogger().name)

    override fun onEnable() {
        try {
            config = ConfigLoader.load(dataFolder.toPath(), logger)
        } catch (ex: IOException) {
            logger.error("Error while creating or loading config. Disabling plugin...", ex)
            return
        }

        try {
            connectDB()
        } catch (ex: Exception) {
            logger.error("Error while connecting to ${database.dbName}. Disabling plugin...", ex)
            return
        }

        val commandAliases = config.plugin.commandAliases
        val command = OnlineTimeCommand(this, commandAliases[0], commandAliases)

        proxy.pluginManager.registerCommand(this, command)
        proxy.pluginManager.registerListener(this, OnlineTimeListener(this))

        if (config.plugin.usePlaceholderApi) {
            proxy.registerChannel(Messaging.CHANNEL_MAIN)
            proxy.registerChannel(Messaging.CHANNEL_TOP)

            val timerInterval = config.plugin.placeholderRefreshSeconds
            if (timerInterval > 0) {
                proxy.scheduler.schedule(this, {
                    for (player in proxy.players) {
                        val onlineTimePlayer = onlineTimePlayers[player.uniqueId] ?: continue
                        val arr = Messaging.createMainArr(onlineTimePlayer, player.uniqueId)
                        if (arr != null)
                            player.server?.sendData(Messaging.CHANNEL_MAIN, arr)
                    }

                    val arr = Messaging.createTopArr(this)
                    for ((_, server) in proxy.servers) {
                        server.sendData(Messaging.CHANNEL_TOP, arr)
                    }

                }, 0L, timerInterval.toLong(), TimeUnit.SECONDS)
            }
        }

        proxy.scheduler.schedule(this, {
            for (player in proxy.players) {
                val otp = onlineTimePlayers[player.uniqueId] ?: continue
                val time = ((otp.savedOnlineTime ?: 0) + otp.getSessionOnlineTime()).floorDiv(60_000).toInt()
                val reward = config.rewards[time] ?: continue
                logger.info("Processing playtime reward for player ${player.name} with $time minutes")

                for (cmd in reward.commands) {
                    val cmd1 = cmd.replace("%PLAYER%", player.name)
                    proxy.pluginManager.dispatchCommand(proxy.console, cmd1)
                }
                for (msg in reward.messages) {
                    val msg1 = msg.replace("%PLAYER%", player.name)
                    player.sendMessage(TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', msg1)))
                }
            }
        }, 5L, 60L, TimeUnit.SECONDS)
    }
}