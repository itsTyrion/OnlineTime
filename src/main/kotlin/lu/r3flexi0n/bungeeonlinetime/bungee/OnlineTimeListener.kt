package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Utils
import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

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
        if (usePlaceholderApi)
            asyncTask(
                doTask = { plugin.database.getOnlineTime(uuid.toString()) },
                onSuccess = { onlineTimePlayer.setSavedOnlineTime(if (it.isNotEmpty()) it[0].time else 0L) },
                onError = { logger.error("Error while loading online time for player ${player.name}.", it) }
            )
    }

    @EventHandler
    fun onSwitch(e: ServerSwitchEvent) {
        val player = e.player
        val onlineTimePlayer = plugin.onlineTimePlayers[player.uniqueId] ?: return
        val server = player.server
        if (disabledServers.contains(server.info.name)) {
            onlineTimePlayer.joinDisabledServer()
        } else {
            onlineTimePlayer.leaveDisabledServer()
        }
        if (usePlaceholderApi && onlineTimePlayer.savedOnlineTime != null) {
            val arr = Utils.createPluginMessageArr(onlineTimePlayer, player.uniqueId)
            server.sendData(plugin.pluginMessageChannel, arr)
        }
    }

    @EventHandler
    fun onLeave(e: PlayerDisconnectEvent) {
        val player = e.player
        val uuid = player.uniqueId
        val onlinePlayer = plugin.onlineTimePlayers.remove(uuid) ?: return
        val time = onlinePlayer.getSessionOnlineTime()
        if (time > 5000L) {
            asyncTask(
                doTask = { plugin.database.updateOnlineTime(uuid.toString(), player.name, time) },
                onSuccess = {},
                onError = { logger.error("Error while saving online time for player ${player.name}.", it) }
            )
        }
    }
}