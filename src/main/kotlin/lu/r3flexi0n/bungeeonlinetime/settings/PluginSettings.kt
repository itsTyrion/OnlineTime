package lu.r3flexi0n.bungeeonlinetime.settings

import java.io.IOException

class PluginSettings(private val settingsFile: SettingsFile) {
    @Throws(IOException::class)
    fun setDefaultSettings() {
        val config = settingsFile.loadConfig()
        for ((key, value) in defaultSettings()) {
            settingsFile.addDefault(config, key, value)
        }
        settingsFile.saveConfig(config)
    }

    @Throws(IOException::class)
    fun loadSettings(): HashMap<String, Any> {
        val settings = HashMap<String, Any>()
        val config = settingsFile.loadConfig()
        for (key in defaultSettings().keys) {
            settings[key] = config[key]
        }
        return settings
    }

    private fun defaultSettings(): HashMap<String, Any> {
        val settings = HashMap<String, Any>()
        settings.putAll(defaultPluginSettings())
        settings.putAll(defaultMySQLSettings())
        settings.putAll(defaultLanguageSettings())
        return settings
    }

    // @formatter:off
    private fun defaultPluginSettings() = mapOf(
        "Plugin.commandAliases"         to arrayListOf("onlinetime", "ot"),
        "Plugin.disabledServers"        to arrayListOf("lobby-1", "lobby-2"),
        "Plugin.topOnlineTimePageLimit" to 10,
        "Plugin.usePlaceholderApi"      to false
    )

    private fun defaultMySQLSettings() = mapOf(
        "MySQL.enabled"  to false,
        "MySQL.host"     to "localhost",
        "MySQL.port"     to 3306,
        "MySQL.database" to "minecraft",
        "MySQL.username" to "player",
        "MySQL.password" to "abc123"
    )

    private fun defaultLanguageSettings() = hashMapOf(
        "Language.onlyPlayer"     to "&7This command can only be executed by players.",
        "Language.noPermission"   to "&7You do not have access to this command.",
        "Language.error"          to "&7An error occured.",
        "Language.playerNotFound" to "&7Player '&6%PLAYER%&7' was not found.",
        "Language.onlineTime"     to "&6%PLAYER%&7's onlinetime: &6%HOURS%&7h &6%MINUTES%&7min",
        "Language.topTimeAbove"   to "&7====== &6Top 10 &7======",
        "Language.topTime"        to "&7#%RANK% &6%PLAYER%&7: &6%HOURS%&7h &6%MINUTES%&7min",
        "Language.topTimeBelow"   to "&7====== &6Page %PAGE% &7======",
        "Language.resetAll"       to "&7The database has been reset.",
        "Language.resetPlayer"    to "&6%PLAYER%&7's onlinetime has been reset.",
        "Language.help"           to "&7Usage:\n" +
                                     "&7/onlinetime\n" +
                                     "&7/onlinetime get <player>\n" +
                                     "&7/onlinetime top [page]\n" +
                                     "&7/onlinetime reset <player>\n" +
                                     "&7/onlinetime resetall"
    )
}