package lu.r3flexi0n.bungeeonlinetime

import lu.r3flexi0n.bungeeonlinetime.utils.AsyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import java.time.Duration
import java.util.logging.Logger
import kotlin.math.max

class OnlineTimeCommand(private val plugin: BungeeOnlineTimePlugin, command: String, vararg aliases: String) :
    Command(command, null, *aliases) {
    private val logger: Logger = plugin.logger
    private val topOnlineTimePageLimit = plugin.settings["Plugin.topOnlineTimePageLimit"] as Int

    private fun checkPermission(sender: CommandSender, permission: String): Boolean {
        if (!sender.hasPermission(permission)) {
            sendMessage(sender, "Language.noPermission")
            return false
        }
        return true
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is ProxiedPlayer) {
            sendMessage(sender, "Language.onlyPlayer")
            return
        }
        val arg0 = if (args.isNotEmpty()) args[0].lowercase() else ""
        val size = args.size

        if (size == 0) {
            if (checkPermission(sender, "onlinetime.own")) {
                val name = sender.getName()
                sendOnlineTime(sender, name)
            }

        } else if (size == 2 && arg0 == "get") {

            if (checkPermission(sender, "onlinetime.others")) {
                val name = args[1]
                sendOnlineTime(sender, name)
            }

        } else if ((size == 1 || size == 2) && arg0 == "top") {

            if (checkPermission(sender, "onlinetime.top")) {
                val page = max(args[1].toIntOrNull() ?: 1, 1)
                sendTopOnlineTimes(sender, page)
            }

        } else if (args.size == 2 && arg0 == "reset") {

            if (checkPermission(sender, "onlinetime.reset")) {
                val name = args[1]
                sendReset(sender, name)
            }

        } else if (args.size == 1 && arg0 == "resetall") {

            if (checkPermission(sender, "onlinetime.resetall"))
                sendResetAll(sender)

        } else {
            sendMessage(sender, "Language.help")
        }
    }

    private fun sendOnlineTime(player: ProxiedPlayer, targetPlayerName: String) {
        AsyncTask(plugin).execute(doTask = {
            plugin.database.getOnlineTime(targetPlayerName)

        }, onSuccess@{ response ->
            if (response.isEmpty()) {
                val placeholders = mapOf("%PLAYER%" to targetPlayerName)
                sendMessage(player, "Language.playerNotFound", placeholders)
                return@onSuccess
            }
            for (onlineTime in response) {
                val sessionTime = plugin.onlineTimePlayers[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(
                    "%PLAYER%" to onlineTime.name,
                    "%HOURS%" to total.toHoursPart(),
                    "%MINUTES%" to total.toMinutesPart()
                )
                sendMessage(player, "Language.onlineTime", placeholders)
            }
        }, onError = { exception ->
            sendMessage(player, "Language.error")
            logger.severe("Error while loading online time for player $targetPlayerName.")
            exception.printStackTrace()
        })
    }

    private fun sendTopOnlineTimes(player: ProxiedPlayer, page: Int) {
        AsyncTask(plugin).execute(doTask = {
            plugin.database.getTopOnlineTimes(page, topOnlineTimePageLimit)

        }, onSuccess = { response ->
            var rank = (page - 1) * topOnlineTimePageLimit + 1
            val headerPlaceholders = mapOf("%PAGE%" to page)
            sendMessage(player, "Language.topTimeAbove", headerPlaceholders)

            for (onlineTime in response) {
                val sessionTime = plugin.onlineTimePlayers[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(
                    "%RANK%" to rank,
                    "%PLAYER%" to onlineTime.name,
                    "%HOURS%" to total.toHoursPart(),
                    "%MINUTES%" to total.toMinutesPart()
                )
                sendMessage(player, "Language.topTime", placeholders)
                rank++
            }
            sendMessage(player, "Language.topTimeBelow", headerPlaceholders)
        }, onError = { exception ->
            sendMessage(player, "Language.error")
            logger.severe("Error while loading top online times.")
            exception.printStackTrace()
        })
    }

    private fun sendReset(player: ProxiedPlayer, targetPlayerName: String) {
        AsyncTask(plugin).execute(doTask = {
            null
        }, onSuccess = {
            sendMessage(player, "Language.resetPlayer", mapOf("%PLAYER%" to targetPlayerName))
        }, onError = { exception ->

            sendMessage(player, "Language.error")
            logger.severe("Error while resetting online time for player $targetPlayerName.")
            exception.printStackTrace()
        })
    }

    private fun sendResetAll(player: ProxiedPlayer) {
        AsyncTask(plugin).execute(doTask = {
            plugin.database.resetAllOnlineTimes()
        }, onSuccess = {
            sendMessage(player, "Language.resetAll")
        }, onError = { ex ->
            sendMessage(player, "Language.error")
            logger.severe("Error while resetting online time database.")
            ex.printStackTrace()
        })
    }

    private fun sendMessage(sender: CommandSender, message: String) {
        sendMessage(sender, message, emptyMap())
    }

    private fun sendMessage(sender: CommandSender, messageId: String, placeholders: Map<String, Any>) {
        var message = plugin.settings[messageId] as String
        for ((key, value) in placeholders) {
            message = message.replace(key, value.toString())
        }
        message =  ChatColor.translateAlternateColorCodes('&', message)
        sender.sendMessage(*TextComponent.fromLegacyText(message))
    }
}