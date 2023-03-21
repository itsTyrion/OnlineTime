package lu.r3flexi0n.bungeeonlinetime.utils

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class AsyncTask(private val plugin: Plugin) {

    private fun <T> execute(task: Task<T>, retry: Boolean) {
        ProxyServer.getInstance().scheduler.runAsync(plugin) {
            try {
                task.onSuccess(task.doTask())
            } catch (ex: Exception) {
                if (retry) {
                    execute(task, false)
                    return@runAsync
                }
                task.onError(ex)
            }
        }
    }

    fun <T> execute(task: Task<T>) = execute(task, true)

    fun <T> execute(doTask: () -> T, onSuccess: (T) -> Unit, onError: (Exception) -> Unit) {
        execute(object : Task<T> {
            @Throws(Exception::class)
            override fun doTask() = doTask()

            override fun onSuccess(response: T) = onSuccess(response)

            override fun onError(exception: Exception) = onError(exception)
        }, true)
    }

    interface Task<T> {
        @Throws(Exception::class)
        fun doTask(): T

        fun onSuccess(response: T)

        fun onError(exception: Exception)
    }
}