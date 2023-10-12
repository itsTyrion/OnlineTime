package lu.r3flexi0n.bungeeonlinetime.common.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.Executors


private val service = Executors.newCachedThreadPool(
    ThreadFactoryBuilder().setNameFormat("OnlineTime Scheduler Thread #%1\$d").build()
)

fun <T> asyncTask(doTask: () -> T, onSuccess: (T) -> Unit, onError: (Exception) -> Unit, maxRetries: Int = 2) {
    var retries = maxRetries
    service.execute {
        try {
            onSuccess(doTask())
        } catch (ex: Exception) {
            if (--retries > 0) {
                asyncTask(doTask, onSuccess, onError, retries)
            } else
                onError(ex)
        }
    }
}