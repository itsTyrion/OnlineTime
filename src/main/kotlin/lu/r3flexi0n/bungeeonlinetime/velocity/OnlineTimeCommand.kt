package lu.r3flexi0n.bungeeonlinetime.velocity

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import lu.r3flexi0n.bungeeonlinetime.common.OnlineTimeCommandBase
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import kotlin.math.max

class OnlineTimeCommand(private val plugin: VelocityOnlineTimePlugin) : SimpleCommand {

    private val config = plugin.config
    private val base = OnlineTimeCommandBase(plugin.logger, plugin.config, plugin.database) { plugin.onlineTimePlayers }

    private fun checkPermission(sender: CommandSource, permission: String): Boolean {
        if (!sender.hasPermission(permission)) {
            sendMessage(sender, config.language.noPermission)
            return false
        }
        return true
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()
        if (sender !is Player) {
            sendMessage(sender, config.language.onlyPlayer)
            return
        }
        val args = invocation.arguments()
        val arg0 = if (args.isNotEmpty()) args[0].lowercase() else ""
        val size = args.size

        if (size == 0) {
            if (checkPermission(sender, "onlinetime.own")) {
                val name = sender.username
//                sendOnlineTime(sender, name)
                base.sendOnlineTime(name) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
            }

        } else if (size == 2 && arg0 == "get") {

            if (checkPermission(sender, "onlinetime.others")) {
                val name = args[1]
//                sendOnlineTime(sender, name)
                base.sendOnlineTime(name) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
            }

        } else if ((size == 1 || size == 2) && arg0 == "top") {

            if (checkPermission(sender, "onlinetime.top")) {
                val page = max(args[1].toIntOrNull() ?: 1, 1)
//                sendTopOnlineTimes(sender, page)
                base.sendTopOnlineTimes(page) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
            }

        } else if (args.size == 2 && arg0 == "reset") {

            if (checkPermission(sender, "onlinetime.reset")) {
                val name = args[1]
//                sendReset(sender, name)
                base.sendReset(name) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
            }

        } else if (args.size == 1 && arg0 == "resetall") {

            if (checkPermission(sender, "onlinetime.resetall"))
//                sendResetAll(sender)
                base.sendResetAll { msg, placeholders -> sendMessage(sender, msg, placeholders) }

        } else {
            sendMessage(sender, config.language.help)
        }
    }

    /*private fun sendOnlineTime(player: Player, targetPlayerName: String) {
        asyncTask(doTask = {
            plugin.database.getOnlineTime(targetPlayerName)

        }, onSuccess@{ response ->
            if (response.isEmpty()) {
                val placeholders = mapOf("%PLAYER%" to targetPlayerName)
                sendMessage(player, config.language.playerNotFound, placeholders)
                return@onSuccess
            }
            for (onlineTime in response) {
                val sessionTime = plugin.onlineTimePlayers[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(
                    "%PLAYER%" to onlineTime.name,
                    "%HOURS%" to total.toHours() % 24,
                    "%MINUTES%" to total.toMinutes() % 60
                )
                sendMessage(player, config.language.onlineTime, placeholders)
            }
        }, onError = { e ->
            sendMessage(player, config.language.error)
            logger.error("Error while loading online time for player $targetPlayerName.", e)
        })
    }*/

    /*private fun sendTopOnlineTimes(player: Player, page: Int) {
        val topOnlineTimePageLimit = config.plugin.topOnlineTimePageLimit
        AsyncTask().exec(doTask = {
            plugin.database.getTopOnlineTimes(page, topOnlineTimePageLimit)

        }, onSuccess = { response ->
            var rank = (page - 1) * topOnlineTimePageLimit + 1
            val headerPlaceholders = mapOf("%PAGE%" to page)
            sendMessage(player, config.language.topTimeAbove, headerPlaceholders)

            for (onlineTime in response) {
                val sessionTime = plugin.onlineTimePlayers[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(
                    "%RANK%" to rank,
                    "%PLAYER%" to onlineTime.name,
                    "%HOURS%" to total.toHours() % 24,
                    "%MINUTES%" to total.toMinutes() % 60
                )
                sendMessage(player, config.language.topTime, placeholders)
                rank++
            }
            sendMessage(player, config.language.topTimeBelow, headerPlaceholders)
        }, onError = { e ->
            sendMessage(player, config.language.error)
            logger.error("Error while loading top online times.", e)
        })
    }*/

    /*private fun sendReset(player: Player, targetPlayerName: String) {
        AsyncTask().exec(doTask = {
            null
        }, onSuccess = {
            sendMessage(player, config.language.resetPlayer, mapOf("%PLAYER%" to targetPlayerName))
        }, onError = { e ->

            sendMessage(player, config.language.error)
            logger.error("Error while resetting online time for player $targetPlayerName.", e)
        })
    }*/

    /*private fun sendResetAll(player: Player) {
        AsyncTask().exec(doTask = {
            plugin.database.resetAllOnlineTimes()
        }, onSuccess = {
            sendMessage(player, config.language.resetAll)
        }, onError = { e ->
            sendMessage(player, config.language.error)
            logger.error("Error while resetting online time database.", e)
        })
    }*/

    private fun sendMessage(sender: CommandSource, messageId: String, placeholders: Map<String, Any>? = emptyMap()) {
        var message = messageId
        if (placeholders != null) {
            for ((key, value) in placeholders) {
                message = message.replace(key, value.toString())
            }
        }
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(messageId))
    }
}