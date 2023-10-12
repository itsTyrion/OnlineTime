package lu.r3flexi0n.bungeeonlinetime.bungee

import lu.r3flexi0n.bungeeonlinetime.common.OnlineTimeCommandBase
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

class OnlineTimeCommand(private val plugin: BungeeOnlineTimePlugin, cmd: String, aliases: Array<String>) :
    Command(cmd, null, *aliases) {
    private val config = plugin.config
    private val base = OnlineTimeCommandBase(plugin.logger, plugin.config, plugin.database) { plugin.onlineTimePlayers }

    private fun checkPermission(sender: CommandSender, permission: String): Boolean {
        if (!sender.hasPermission(permission)) {
            send(sender, config.language.noPermission)
            return false
        }
        return true
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is ProxiedPlayer) {
            send(sender, config.language.onlyPlayer)
            return
        }
        val arg0 = if (args.isNotEmpty()) args[0].lowercase() else ""
        val size = args.size

        if (size == 0) {
            if (checkPermission(sender, "onlinetime.own")) {
                base.sendOnlineTime(sender.getName()) { msg, placeholders -> send(sender, msg, placeholders) }
            }

        } else if (size == 2 && arg0 == "get") {

            if (checkPermission(sender, "onlinetime.others")) {
                val name = args[1]
                base.sendOnlineTime(name) { msg, placeholders -> send(sender, msg, placeholders) }
            }

        } else if ((size == 1 || size == 2) && arg0 == "top") {

            if (checkPermission(sender, "onlinetime.top")) {
                val page = (args[1].toIntOrNull() ?: 1).coerceAtLeast(1)
                base.sendTopOnlineTimes(page) { msg, placeholders -> send(sender, msg, placeholders) }
            }

        } else if (args.size == 2 && arg0 == "reset") {

            if (checkPermission(sender, "onlinetime.reset")) {
                val name = args[1]
                base.sendReset(name) { msg, placeholders -> send(sender, msg, placeholders) }
            }

        } else if (args.size == 1 && arg0 == "resetall") {

            if (checkPermission(sender, "onlinetime.resetall"))
                base.sendResetAll { msg, placeholders -> send(sender, msg, placeholders) }

        } else {
            send(sender, config.language.help)
        }
    }


    private fun send(sender: CommandSender, messageId: String, placeholders: Map<String, Any>? = null) {
        var message = messageId
        if (placeholders != null) {
            for ((key, value) in placeholders) {
                message = message.replace(key, value.toString())
            }
        }
        sender.sendMessage(*TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)))
    }
}