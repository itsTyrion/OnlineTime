package lu.r3flexi0n.bungeeonlinetime.common.config

class Config {
    @JvmField var language = Language()

    @JvmField var plugin = Plugin()

    @JvmField var mySQL = MySQL()

    @Suppress("PropertyName")
    var version_dont_touch = CURRENT_VERSION

    class Language { // @formatter:on
        var help = """
             &7Usage:
             &7/onlinetime
             &7/onlinetime get <player>
             &7/onlinetime top [page]
             &7/onlinetime reset <player>
             &7/onlinetime resetall
             """.trimIndent() // @formatter:off
        var resetAll       = "&bThe database has been reset."
        var playerNotFound = "&cPlayer '&6%PLAYER%&c' was not found."
        var topTimeAbove   = "&7====== &6Top 10 &7======"
        var onlineTime     = "&6%PLAYER%&7's onlinetime: &6%HOURS%&7h &6%MINUTES%&7min"
        var topTimeBelow   = "&7====== &6Page %PAGE% &7======"
        var noPermission   = "&cYou do not have access to this command."
        var topTime        = "&7#%RANK% &6%PLAYER%&7: &6%HOURS%&7h &6%MINUTES%&7min"
        var error          = "&cAn error occurred."
        var onlyPlayer     = "&cThis command can only be executed by players."
        var resetPlayer    = "&6%PLAYER%&b's onlinetime has been reset."
        var configReloaded = "&bThe config has been reloaded. A restart is still recommended."
    } // @formatter:on

    class Plugin {
        var commandAliases = arrayOf("onlinetime", "ot")
        var disabledServers = mutableListOf("lobby-1", "lobby-2")
        var usePlaceholderApi = false
        var topOnlineTimePageLimit = 10
        @Deprecated("Replaced. Minutes -> Seconds", ReplaceWith("placeholderRefreshSeconds"))
        var placeholderRefreshTimer = Int.MIN_VALUE
        var placeholderRefreshSeconds = 60
    }

    class MySQL { // @formatter:off
        var enabled  = false
        var host     = "localhost"
        var port     = 3306
        var database = "minecraft"
        var username = "onlinetime"
        var password = "abc123"
    } // @formatter:on

    var rewards: Map<Int, RewardAction> = mapOf(
        1 to RewardAction(
            messages = listOf("&aYou just spent your first minute here \\o/")
        ),
        2 to RewardAction(
            commands = listOf("exampleCommandRanAtTwoMinutes %PLAYER%"),
        ),
        10 to RewardAction(
            messages = listOf("&aHope you enjoyed the first 10 minutes! Here's 100 bucks, on the house."),
            commands = listOf("eco give %PLAYER% 100")
        )
    )

    class RewardAction(
        var commands: List<String> = listOf(),
        var messages: List<String> = listOf()
    )

    companion object {
        const val CURRENT_VERSION = 3
    }
}