package lu.r3flexi0n.bungeeonlinetime

import lu.r3flexi0n.bungeeonlinetime.database.Database
import lu.r3flexi0n.bungeeonlinetime.database.MySQLDatabase
import lu.r3flexi0n.bungeeonlinetime.database.SQLiteDatabase
import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTimePlayer
import lu.r3flexi0n.bungeeonlinetime.settings.PluginSettings
import lu.r3flexi0n.bungeeonlinetime.settings.SettingsFile
import net.md_5.bungee.api.plugin.Plugin
import java.io.File
import java.util.*

class BungeeOnlineTimePlugin : Plugin() {

    lateinit var settings: HashMap<String, Any>

    lateinit var database: Database

    val onlineTimePlayers = HashMap<UUID, OnlineTimePlayer>()

    val pluginMessageChannel = "bungeeonlinetime:get"

    override fun onEnable() {
        val settingsFile = SettingsFile(File(dataFolder, "settings.yml"))
        try {
            settingsFile.create()
        } catch (ex: Exception) {
            logger.severe("Error while creating settings file. Disabling plugin...")
            ex.printStackTrace()
            return
        }

        val pluginSettings = PluginSettings(settingsFile)
        settings = try {
            pluginSettings.setDefaultSettings()
            pluginSettings.loadSettings()
        } catch (ex: Exception) {
            logger.severe("Error while loading settings file. Disabling plugin...")
            ex.printStackTrace()
            return
        }

        val mysqlEnabled = settings["MySQL.enabled"] as Boolean
        database = if (mysqlEnabled) {
            val host = settings["MySQL.host"] as String
            val port = settings["MySQL.port"] as Int
            val db =   settings["MySQL.database"] as String
            val user = settings["MySQL.username"] as String
            val pass = settings["MySQL.password"] as String
            MySQLDatabase(host, port, db, user, pass)
        } else {
            val databaseFile = File(dataFolder, "BungeeOnlineTime.db")
            SQLiteDatabase(databaseFile)
        }

        try {
            logger.info("Connecting to " + database.databaseName + "...")
            database.openConnection()
            database.createTable()
            database.createIndex()
            logger.info("Successfully connected to " + database.databaseName + ".")
        } catch (ex: Exception) {
            logger.severe("Error while connecting to " + database.databaseName + ". Disabling plugin...")
            ex.printStackTrace()
            return
        }

        val commandAliases = (settings["Plugin.commandAliases"] as List<String>).toTypedArray()
        val command = OnlineTimeCommand(this, commandAliases[0], *commandAliases)

        proxy.pluginManager.registerCommand(this, command)
        proxy.pluginManager.registerListener(this, OnlineTimeListener(this))

        if (settings["Plugin.usePlaceholderApi"] as Boolean) {
            proxy.registerChannel(pluginMessageChannel)
        }
    }
}