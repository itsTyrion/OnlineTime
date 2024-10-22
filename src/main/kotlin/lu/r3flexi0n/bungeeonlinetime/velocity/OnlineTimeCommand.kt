package lu.r3flexi0n.bungeeonlinetime.velocity

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import lu.r3flexi0n.bungeeonlinetime.common.OnlineTimeCommandBase
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import kotlin.math.max

class OnlineTimeCommand(private val plugin: VelocityOnlineTimePlugin) : SimpleCommand {

    private val base = OnlineTimeCommandBase(plugin)

    private fun checkPermission(sender: CommandSource, permission: String): Boolean {
        if (!sender.hasPermission(permission)) {
            send(sender, plugin.config.language.noPermission)
            return false
        }
        return true
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()
        if (sender !is Player) {
            send(sender, plugin.config.language.onlyPlayer)
            return
        }
        val args = invocation.arguments()
        val arg0 = if (args.isNotEmpty()) args[0].lowercase() else ""
        val size = args.size

        if (size == 0) {
            if (checkPermission(sender, "onlinetime.own")) {
                base.sendOnlineTime(sender.username) { msg, placeholders -> send(sender, msg, placeholders) }
            }

        } else if (size == 2 && arg0 == "get") {

            if (checkPermission(sender, "onlinetime.others")) {
                val name = args[1]
                base.sendOnlineTime(name) { msg, placeholders -> send(sender, msg, placeholders) }
            }

        } else if ((size == 1 || size == 2) && arg0 == "top") {

            if (checkPermission(sender, "onlinetime.top")) {
                val page = if (size == 1) 1 else max(1, args[1].toIntOrNull() ?: 1)
                base.sendTopOnlineTimes(page) { msg, placeholders -> send(sender, msg, placeholders) }
            }

        } else if (args.size == 2 && arg0 == "reset") {

            if (checkPermission(sender, "onlinetime.reset")) {
                val name = args[1]
                base.sendReset(name) { msg, placeholders -> send(sender, msg, placeholders) }
                plugin.proxy.getPlayer(name)
                    .ifPresent { plugin.onlineTimePlayers[it.uniqueId]?.setSavedOnlineTime(0L) }
            }

        } else if (args.size == 1 && arg0 == "resetall") {

            if (checkPermission(sender, "onlinetime.resetall"))
                base.sendResetAll { msg, placeholders -> send(sender, msg, placeholders) }

        } else if (args.size == 1 && arg0 == "reload") {

            if (checkPermission(sender, "onlinetime.reload")) {
                plugin.reloadConfig()
                send(sender, plugin.config.language.configReloaded)
            }

        }  else {
            send(sender, plugin.config.language.help)
        }
    }

    private fun send(sender: CommandSource, messageId: String, placeholders: Map<String, Any>? = null) {
        var message = messageId
        if (placeholders != null) {
            for ((key, value) in placeholders) {
                message = message.replace(key, value.toString())
            }
        }
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
    }
}