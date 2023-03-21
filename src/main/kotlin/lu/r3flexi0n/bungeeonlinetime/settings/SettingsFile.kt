package lu.r3flexi0n.bungeeonlinetime.settings

import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.IOException

class SettingsFile(private val file: File) {

    private val provider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)

    @Throws(Exception::class)
    fun create() {
        val directory = file.parentFile
        if (directory != null && !directory.exists()) {
            val success = directory.mkdirs()
            if (!success) {
                throw Exception()
            }
        }

        if (!file.exists()) {
            val success = file.createNewFile()
            if (!success) {
                throw Exception()
            }
        }
    }

    @Throws(IOException::class)
    fun loadConfig(): Configuration {
        return provider.load(file)
    }

    @Throws(IOException::class)
    fun saveConfig(config: Configuration) {
        provider.save(config, file)
    }

    fun addDefault(config: Configuration, key: String, value: Any?) {
        if (!config.contains(key)) {
            config[key] = value
        }
    }
}