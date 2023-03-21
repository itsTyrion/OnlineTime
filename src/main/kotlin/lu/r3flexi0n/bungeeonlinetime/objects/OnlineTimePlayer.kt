package lu.r3flexi0n.bungeeonlinetime.objects

class OnlineTimePlayer {
    var savedOnlineTime: Long? = null
        private set
    private val joinProxyTimestamp = System.currentTimeMillis()
    private var timeOnDisabledServers = 0L
    private var joinDisabledServerTimestamp = 0L

    fun setSavedOnlineTime(onlineTime: Long) {
        savedOnlineTime = onlineTime
    }

    fun getSessionOnlineTime() =
        System.currentTimeMillis() - joinProxyTimestamp - getTimeOnDisabledServers()

    private fun getTimeOnDisabledServers(): Long {
        var time = timeOnDisabledServers
        if (joinDisabledServerTimestamp > 0) {
            time += System.currentTimeMillis() - joinDisabledServerTimestamp
        }
        return time
    }

    fun joinDisabledServer() {
        if (joinDisabledServerTimestamp == 0L) {
            joinDisabledServerTimestamp = System.currentTimeMillis()
        }
    }

    fun leaveDisabledServer() {
        if (joinDisabledServerTimestamp > 0) {
            timeOnDisabledServers += System.currentTimeMillis() - joinDisabledServerTimestamp
            joinDisabledServerTimestamp = 0
        }
    }
}