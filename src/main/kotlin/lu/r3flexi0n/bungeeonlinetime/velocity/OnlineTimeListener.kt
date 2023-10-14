package lu.r3flexi0n.bungeeonlinetime.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Utils
import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask

class OnlineTimeListener(private val plugin: VelocityOnlineTimePlugin) {
    private val logger = plugin.logger
    private val disabledServers get() = plugin.config.plugin.disabledServers
    private val usePlaceholderApi get() = plugin.config.plugin.usePlaceholderApi

    @Subscribe
    fun onJoin(e: PostLoginEvent) {
        val player = e.player
        if (!player.hasPermission("onlinetime.save")) {
            return
        }
        val uuid = player.uniqueId
        val onlineTimePlayer = OnlineTimePlayer()
        plugin.onlineTimePlayers[uuid] = onlineTimePlayer
        if (usePlaceholderApi) {
            val name = player.username
            asyncTask(
                doTask = { plugin.database.getOnlineTime(uuid.toString()) },
                onSuccess = { onlineTimes ->

                    val savedOnlineTime = if (onlineTimes.isNotEmpty()) onlineTimes[0].time else 0L
                    onlineTimePlayer.setSavedOnlineTime(savedOnlineTime)
                    val arr = Utils.createPluginMessageArr(onlineTimePlayer, player.uniqueId)
                    player.currentServer.get().sendPluginMessage(plugin.pluginMessageChannel, arr)
                },
                onError = { ex ->
                    logger.error("Error while loading online time for player $name.")
                    ex.printStackTrace()
                }
            )
        }
    }

    @Subscribe
    fun onSwitch(e: ServerConnectedEvent) {
        val player = e.player
        val onlineTimePlayer = plugin.onlineTimePlayers[player.uniqueId] ?: return
        val server = e.server.serverInfo
        if (disabledServers.contains(server.name)) {
            onlineTimePlayer.joinDisabledServer()
        } else {
            onlineTimePlayer.leaveDisabledServer()
        }
        if (usePlaceholderApi) {
            val savedOnlineTime = onlineTimePlayer.savedOnlineTime
            if (savedOnlineTime != null) {
                val arr = Utils.createPluginMessageArr(onlineTimePlayer, player.uniqueId)
                player.currentServer.get().sendPluginMessage(plugin.pluginMessageChannel, arr)
            }
        }
    }

    @Subscribe
    fun onLeave(e: DisconnectEvent) {
        val player = e.player
        val uuid = player.uniqueId
        val onlinePlayer = plugin.onlineTimePlayers[uuid] ?: return
        plugin.onlineTimePlayers.remove(uuid)
        val time = onlinePlayer.getSessionOnlineTime()
        if (time < 5000L) {
            return
        }
        val name = player.username
        asyncTask(
            doTask = { plugin.database.updateOnlineTime(uuid.toString(), name, time) },
            onSuccess = {},
            onError = { ex -> logger.error("Error while saving online time for player $name.", ex) }
        )
    }
}