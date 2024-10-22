package lu.r3flexi0n.bungeeonlinetime.common

import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask
import java.time.Duration
import java.util.function.BiConsumer

class OnlineTimeCommandBase(private val plugin: OnlineTimePlugin) {
    private val config get() = plugin.config

    fun sendOnlineTime(targetPlayerName: String, sendMessage: BiConsumer<String, Map<String, Any>?>) {
        asyncTask(doTask = {
            plugin.database.getOnlineTime(targetPlayerName)

        }, onSuccess = { response ->
            if (response.isEmpty()) {
                val placeholders = mapOf("%PLAYER%" to targetPlayerName)
                sendMessage.accept(config.language.playerNotFound, placeholders)
            } else {
                for (onlineTime in response) {
                    val sessionTime = plugin.onlineTimePlayers[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                    val total = Duration.ofMillis(onlineTime.time + sessionTime)

                    val placeholders = mapOf(//@formatter:off
                        "%PLAYER%"  to onlineTime.name,
                        "%HOURS%"   to total.toHours(),
                        "%MINUTES%" to total.toMinutesPart()
                    )//@formatter:on
                    sendMessage.accept(config.language.onlineTime, placeholders)
                }
            }
        }, onError = { e ->
            sendMessage.accept(config.language.error, emptyMap())
            plugin.logger.error("Error while loading online time for player $targetPlayerName.", e)
        })
    }

    fun sendTopOnlineTimes(page: Int, sendMessage: BiConsumer<String, Map<String, Any>?>) {
        val topOnlineTimePageLimit = config.plugin.topOnlineTimePageLimit
        asyncTask(doTask = {
            plugin.database.getTopOnlineTimes(page, topOnlineTimePageLimit)

        }, onSuccess = { response ->
            var rank = (page - 1) * topOnlineTimePageLimit + 1
            val headerPlaceholders = mapOf("%PAGE%" to page)
            sendMessage.accept(config.language.topTimeAbove, headerPlaceholders)

            for (onlineTime in response) {
                val sessionTime = plugin.onlineTimePlayers[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(//@formatter:off
                    "%RANK%"    to rank,
                    "%PLAYER%"  to onlineTime.name,
                    "%HOURS%"   to total.toHours(),
                    "%MINUTES%" to total.toMinutesPart()
                )//@formatter:on
                sendMessage.accept(config.language.topTime, placeholders)
                rank++
            }
            sendMessage.accept(config.language.topTimeBelow, headerPlaceholders)
        }, onError = { e ->
            sendMessage.accept(config.language.error, null)
            plugin.logger.error("Error while loading top online times.", e)
        })
    }

    fun sendReset(targetPlayerName: String, sendMessage: BiConsumer<String, Map<String, Any>?>) {
        asyncTask(doTask = {
            plugin.database.resetOnlineTime(targetPlayerName)
        }, onSuccess = {
            sendMessage.accept(config.language.resetPlayer, mapOf("%PLAYER%" to targetPlayerName))
        }, onError = { e ->
            sendMessage.accept(config.language.error, emptyMap())
            plugin.logger.error("Error while resetting online time for player $targetPlayerName.", e)
        })
    }

    fun sendResetAll(sendMessage: BiConsumer<String, Map<String, Any>?>) {
        asyncTask(doTask = {
            plugin.database.resetAllOnlineTimes()
            plugin.onlineTimePlayers.forEach { (_, value) -> value.setSavedOnlineTime(0L) }
        }, onSuccess = {
            sendMessage.accept(config.language.resetAll, emptyMap())
        }, onError = { e ->
            sendMessage.accept(config.language.error, emptyMap())
            plugin.logger.error("Error while resetting online time database.", e)
        })
    }
}