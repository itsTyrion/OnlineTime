package lu.r3flexi0n.bungeeonlinetime.common.utils

import lu.r3flexi0n.bungeeonlinetime.common.OnlineTimePlugin
import lu.r3flexi0n.bungeeonlinetime.common.objects.OnlineTimePlayer
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.UUID

object Messaging {
    const val CHANNEL_MAIN = "bungeeonlinetime:get"
    const val CHANNEL_TOP = "bungeeonlinetime:top"

    fun createMainArr(otp: OnlineTimePlayer, uuid: UUID): ByteArray? {
        val savedOnlineTime = otp.savedOnlineTime ?: return null
        val totalOnlineTime = savedOnlineTime + otp.getSessionOnlineTime()

        val baos = ByteArrayOutputStream()
        val data = DataOutputStream(baos)
        data.writeUTF(uuid.toString())
        data.writeLong(totalOnlineTime / 1000L)
        return baos.toByteArray()
    }

    fun createTopArr(plugin: OnlineTimePlugin): ByteArray {
        val top = plugin.database.getTopOnlineTimes(1, 1).firstOrNull()
        val baos = ByteArrayOutputStream()
        val data = DataOutputStream(baos)
        if (top == null) {
            data.writeUTF("")
            data.writeLong(0L)
        } else {
            val otp = plugin.onlineTimePlayers[top.uuid]
            val totalOnlineTime = if (otp?.savedOnlineTime != null) {
                otp.savedOnlineTime!! + otp.getSessionOnlineTime()
            } else
                top.time
            data.writeUTF(top.name)
            data.writeLong(totalOnlineTime / 1000L)
        }
        return baos.toByteArray()
    }
}