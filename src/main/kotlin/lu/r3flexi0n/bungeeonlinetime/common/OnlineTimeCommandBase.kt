package lu.r3flexi0n.bungeeonlinetime.common

import lu.r3flexi0n.bungeeonlinetime.common.config.Config
import lu.r3flexi0n.bungeeonlinetime.common.db.Database
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.common.utils.asyncTask
import org.slf4j.Logger
import java.time.Duration
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Supplier

class OnlineTimeCommandBase(
    private val logger: Logger,
    private val config: Config,
    private val database: Database,
    private val onlineTimePlayers: Supplier<Map<UUID, OnlineTimePlayer>>
) {

    fun sendOnlineTime(targetPlayerName: String, sendMessage: BiConsumer<String, Map<String, Any>?>) {
        asyncTask(doTask = {
            database.getOnlineTime(targetPlayerName)

        }, onSuccess@{ response ->
            if (response.isEmpty()) {
                val placeholders = mapOf("%PLAYER%" to targetPlayerName)
                sendMessage.accept(config.language.playerNotFound, placeholders)
                return@onSuccess
            }
            for (onlineTime in response) {
                val sessionTime = onlineTimePlayers.get()[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(
                    "%PLAYER%" to onlineTime.name,
                    "%HOURS%" to total.toHours() % 24,
                    "%MINUTES%" to total.toMinutes() % 60
                )
                sendMessage.accept(config.language.onlineTime, placeholders)
            }
        }, onError = { e ->
            sendMessage.accept(config.language.error, emptyMap())
            logger.error("Error while loading online time for player $targetPlayerName.", e)
        })
    }

     fun sendTopOnlineTimes(page: Int, sendMessage: BiConsumer<String, Map<String, Any>?>) {
        val topOnlineTimePageLimit = config.plugin.topOnlineTimePageLimit
         asyncTask(doTask = {
            database.getTopOnlineTimes(page, topOnlineTimePageLimit)

        }, onSuccess = { response ->
            var rank = (page - 1) * topOnlineTimePageLimit + 1
            val headerPlaceholders = mapOf("%PAGE%" to page)
            sendMessage.accept(config.language.topTimeAbove, headerPlaceholders)

            for (onlineTime in response) {
                val sessionTime = onlineTimePlayers.get()[onlineTime.uuid]?.getSessionOnlineTime() ?: 0

                val total = Duration.ofMillis(onlineTime.time + sessionTime)

                val placeholders = mapOf(
                    "%RANK%" to rank,
                    "%PLAYER%" to onlineTime.name,
                    "%HOURS%" to total.toHours() % 24,
                    "%MINUTES%" to total.toMinutes() % 60
                )
                sendMessage.accept(config.language.topTime, placeholders)
                rank++
            }
            sendMessage.accept(config.language.topTimeBelow, headerPlaceholders)
        }, onError = { e ->
            sendMessage.accept(config.language.error, null)
            logger.error("Error while loading top online times.", e)
        })
    }

    fun sendReset(targetPlayerName: String, sendMessage: BiConsumer<String, Map<String, Any>?>) {
        asyncTask(doTask = {
            database.resetOnlineTime(targetPlayerName)
        }, onSuccess = {
            sendMessage.accept(config.language.resetPlayer, mapOf("%PLAYER%" to targetPlayerName))
        }, onError = { e ->
            sendMessage.accept(config.language.error, emptyMap())
            logger.error("Error while resetting online time for player $targetPlayerName.", e)
        })
    }

    fun sendResetAll(sendMessage: BiConsumer<String, Map<String, Any>?>) {
        asyncTask(doTask = {
            database.resetAllOnlineTimes()
            onlineTimePlayers.get().forEach { (_, value) -> value.setSavedOnlineTime(0L) }
        }, onSuccess = {
            sendMessage.accept(config.language.resetAll, emptyMap())
        }, onError = { e ->
            sendMessage.accept(config.language.error, emptyMap())
            logger.error("Error while resetting online time database.", e)
        })
    }
}