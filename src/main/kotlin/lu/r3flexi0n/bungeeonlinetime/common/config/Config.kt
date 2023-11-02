package lu.r3flexi0n.bungeeonlinetime.common.config

class Config {
    @JvmField var language = Language()

    @JvmField var plugin = Plugin()

    @JvmField var mySQL = MySQL()

    @Suppress("PropertyName", "SpellCheckingInspection") // mb lemme use quotes in a variable name real quick
    var version_dont_touch = 1

    class Language {
        var help = """
             &7Usage:
             &7/onlinetime
             &7/onlinetime get <player>
             &7/onlinetime top [page]
             &7/onlinetime reset <player>
             &7/onlinetime resetall
             """.trimIndent() // @formatter:off
        var resetAll       = "&7The database has been reset."
        var playerNotFound = "&7Player '&6%PLAYER%&7' was not found."
        var topTimeAbove   = "&7====== &6Top 10 &7======"
        var onlineTime     = "&6%PLAYER%&7's onlinetime: &6%HOURS%&7h &6%MINUTES%&7min"
        var topTimeBelow   = "&7====== &6Page %PAGE% &7======"
        var noPermission   = "&7You do not have access to this command."
        var topTime        = "&7#%RANK% &6%PLAYER%&7: &6%HOURS%&7h &6%MINUTES%&7min"
        var error          = "&7An error occurred."
        var onlyPlayer     = "&7This command can only be executed by players."
        var resetPlayer    = "&6%PLAYER%&7's onlinetime has been reset."
    } // @formatter:on

    class Plugin {
        var commandAliases = arrayOf("onlinetime", "ot")
        var disabledServers = mutableListOf("lobby-1", "lobby-2")
        var usePlaceholderApi = false
        var topOnlineTimePageLimit = 10
        var placeholderRefreshTimer = 1
    }

    class MySQL { // @formatter:off
        var enabled  = false
        var host     = "localhost"
        var port     = 3306
        var database = "minecraft"
        var username = "onlinetime"
        var password = "abc123"
    } // @formatter:on

    companion object {
        const val CURRENT_VERSION = 1
    }
}