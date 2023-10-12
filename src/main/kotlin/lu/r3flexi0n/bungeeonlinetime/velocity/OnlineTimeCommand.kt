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
                base.sendOnlineTime(name) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
            }

        } else if (size == 2 && arg0 == "get") {

            if (checkPermission(sender, "onlinetime.others")) {
                val name = args[1]
                base.sendOnlineTime(name) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
            }

        } else if ((size == 1 || size == 2) && arg0 == "top") {

            if (checkPermission(sender, "onlinetime.top")) {
                val page = max(args[1].toIntOrNull() ?: 1, 1)
                base.sendTopOnlineTimes(page) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
            }

        } else if (args.size == 2 && arg0 == "reset") {

            if (checkPermission(sender, "onlinetime.reset")) {
                val name = args[1]
                base.sendReset(name) { msg, placeholders -> sendMessage(sender, msg, placeholders) }
                plugin.proxy.getPlayer(name)
                        .ifPresent { plugin.onlineTimePlayers[it.uniqueId]!!.setSavedOnlineTime(0L) }
            }

        } else if (args.size == 1 && arg0 == "resetall") {

            if (checkPermission(sender, "onlinetime.resetall"))
                base.sendResetAll { msg, placeholders -> sendMessage(sender, msg, placeholders) }

        } else {
            sendMessage(sender, config.language.help)
        }
    }

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