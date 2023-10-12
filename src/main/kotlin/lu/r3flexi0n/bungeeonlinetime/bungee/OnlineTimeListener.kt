package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

class OnlineTimeListener(private val plugin: BungeeOnlineTimePlugin) : Listener {
    private val logger = plugin.logger
    private val disabledServers get() = plugin.config.plugin.disabledServers
    private val usePlaceholderApi get() = plugin.config.plugin.usePlaceholderApi

    @EventHandler
    fun onJoin(e: PostLoginEvent) {
        val player = e.player
        if (!player.hasPermission("onlinetime.save")) {
            return
        }
        val uuid = player.uniqueId
        val onlineTimePlayer = OnlineTimePlayer()
        plugin.onlineTimePlayers[uuid] = onlineTimePlayer
        if (usePlaceholderApi) {
            val name = player.name
            asyncTask(
                doTask = { plugin.database.getOnlineTime(uuid.toString()) },
                onSuccess = { onlineTimes ->

                    val savedOnlineTime = if (onlineTimes.isNotEmpty()) onlineTimes[0].time else 0L
                    onlineTimePlayer.setSavedOnlineTime(savedOnlineTime)
                    sendOnlineTimeToServer(player, savedOnlineTime)
                },
                onError = { ex ->
                    logger.error("Error while loading online time for player $name.", ex)
                }
            )
        }
    }

    @EventHandler
    fun onSwitch(e: ServerSwitchEvent) {
        val player = e.player
        val onlineTimePlayer = plugin.onlineTimePlayers[player.uniqueId] ?: return
        val server = player.server.info
        if (disabledServers.contains(server.name)) {
            onlineTimePlayer.joinDisabledServer()
        } else {
            onlineTimePlayer.leaveDisabledServer()
        }
        if (usePlaceholderApi) {
            val savedOnlineTime = onlineTimePlayer.savedOnlineTime
            if (savedOnlineTime != null) {
                val totalOnlineTime = savedOnlineTime + onlineTimePlayer.getSessionOnlineTime()
                sendOnlineTimeToServer(player, totalOnlineTime)
            }
        }
    }

    @EventHandler
    fun onLeave(e: PlayerDisconnectEvent) {
        val player = e.player
        val uuid = player.uniqueId
        val onlinePlayer = plugin.onlineTimePlayers[uuid] ?: return
        plugin.onlineTimePlayers.remove(uuid)
        val time = onlinePlayer.getSessionOnlineTime()
        if (time < 5000L) {
            return
        }
        val name = player.name
        asyncTask(
            doTask = { plugin.database.updateOnlineTime(uuid.toString(), name, time) },
            onSuccess = {},
            onError = { ex -> logger.error("Error while saving online time for player $name.", ex) }
        )
    }

    private fun sendOnlineTimeToServer(player: ProxiedPlayer, onlineTime: Long) {
        val server = player.server ?: return
        try {
            val out = ByteArrayOutputStream()
            val data = DataOutputStream(out)
            data.writeUTF(player.uniqueId.toString())
            data.writeLong(onlineTime / 1000)
            server.sendData(plugin.pluginMessageChannel, out.toByteArray())
            data.close()
        } catch (ex: IOException) {
            logger.error("Error while sending plugin message.", ex)
        }
    }
}