package lu.r3flexi0n.bungeeonlinetime.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.Messaging
import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask

class OnlineTimeListener(private val plugin: VelocityOnlineTimePlugin) {
    private val logger = plugin.logger

    @Subscribe
    fun onJoin(e: PostLoginEvent) {
        val player = e.player
        if (!player.hasPermission("onlinetime.save")) {
            return
        }
        val uuid = player.uniqueId
        val onlineTimePlayer = OnlineTimePlayer()
        plugin.onlineTimePlayers[uuid] = onlineTimePlayer
        asyncTask(
            doTask = { plugin.database.getOnlineTime(uuid.toString()) },
            onSuccess = { onlineTimePlayer.setSavedOnlineTime(if (it.isNotEmpty()) it[0].time else 0L) },
            onError = { ex ->
                logger.error("Error while loading online time for player ${player.username}.")
                ex.printStackTrace()
            }
        )
    }

    @Subscribe
    @Suppress("UnstableApiUsage")
    fun onSwitch(e: ServerPostConnectEvent) {
        val player = e.player
        val onlineTimePlayer = plugin.onlineTimePlayers[player.uniqueId] ?: return
        val server = player.currentServer.get()
        if (plugin.config.plugin.disabledServers.contains(server.serverInfo.name)) {
            onlineTimePlayer.joinDisabledServer()
        } else {
            onlineTimePlayer.leaveDisabledServer()
        }
        if (plugin.config.plugin.usePlaceholderApi) {
            if (onlineTimePlayer.savedOnlineTime != null) {
                val arr = Messaging.createMainArr(onlineTimePlayer, player.uniqueId)!!
                server.sendPluginMessage(plugin.pluginMessageChannelMain, arr)
            }
            val arr = Messaging.createTopArr(plugin)
            server.sendPluginMessage(plugin.pluginMessageChannelTop, arr)
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