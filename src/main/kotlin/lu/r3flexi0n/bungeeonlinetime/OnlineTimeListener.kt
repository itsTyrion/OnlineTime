package lu.r3flexi0n.bungeeonlinetime

import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.utils.AsyncTask
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
    private val disabledServers = plugin.settings["Plugin.disabledServers"] as List<String>
    private val usePlaceholderApi = plugin.settings["Plugin.usePlaceholderApi"] as Boolean

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
            AsyncTask(plugin).execute(
                doTask = { plugin.database.getOnlineTime(uuid.toString()) },
                onSuccess = { onlineTimes ->

                    val savedOnlineTime = if (onlineTimes.isNotEmpty()) onlineTimes[0].time else 0L
                    onlineTimePlayer.setSavedOnlineTime(savedOnlineTime)
                    sendOnlineTimeToServer(player, savedOnlineTime)
                },
                onError = { ex ->
                    logger.severe("Error while loading online time for player $name.")
                    ex.printStackTrace()
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
        AsyncTask(plugin).execute(
            doTask = { plugin.database.updateOnlineTime(uuid.toString(), name, time) },
            onSuccess = {},
            onError = { ex ->
                logger.severe("Error while saving online time for player $name.")
                ex.printStackTrace()
            }
        )
    }

    private fun sendOnlineTimeToServer(player: ProxiedPlayer?, onlineTime: Long) {
        if (player == null || player.server == null) {
            return
        }
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val dataOutputStream = DataOutputStream(byteArrayOutputStream)
            dataOutputStream.writeUTF(player.uniqueId.toString())
            dataOutputStream.writeLong(onlineTime / 1000)
            player.server.sendData(plugin.pluginMessageChannel, byteArrayOutputStream.toByteArray())
            dataOutputStream.close()
        } catch (ex: IOException) {
            logger.severe("Error while sending plugin message.")
            ex.printStackTrace()
        }
    }
}