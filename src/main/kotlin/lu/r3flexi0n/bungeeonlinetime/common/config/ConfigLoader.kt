package lu.r3flexi0n.bungeeonlinetime.common.config

import org.slf4j.Logger
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.DumperOptions.FlowStyle
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import java.nio.file.Path
import kotlin.io.path.*

object ConfigLoader {

    fun load(dataFolderPath: Path, logger: Logger): Config {
        dataFolderPath.createDirectories()
        val settingsPath = dataFolderPath.resolve("settings.yml")

        if (!settingsPath.exists()) {
            settingsPath.createFile()
            save(Config(), settingsPath)
            logger.warn("No config file was found, default config was created.")
        }
        settingsPath.inputStream().use { input ->
            val config = Yaml().loadAs(input, Config::class.java)
            if (config.version_dont_touch < Config.CURRENT_VERSION) {
                migrateConfig(config)
                save(config, settingsPath)
                logger.warn("New config version, please check new values and change if/as needed.")
            }
            return config
        }
    }

    @Suppress("DEPRECATION")
    private fun migrateConfig(config: Config) {
        if (config.version_dont_touch == 1) {
            config.plugin.placeholderRefreshSeconds = config.plugin.placeholderRefreshTimer * 60
        }
        config.version_dont_touch = Config.CURRENT_VERSION
    }

    private fun save(config: Config, settingsPath: Path) {
        val dumperOptions = DumperOptions()
        dumperOptions.isDereferenceAliases = true
        dumperOptions.defaultFlowStyle = FlowStyle.BLOCK
        val representer = Representer(dumperOptions)

        val typeDescription = TypeDescription(Config.Plugin::class.java)
        typeDescription.setExcludes("placeholderRefreshTimer")
        representer.addTypeDescription(typeDescription)
        // To disable the tag with class name
        representer.addClassTag(Config.Plugin::class.java, Tag.MAP)
        representer.addClassTag(Config::class.java, Tag.MAP)

        val yaml = Yaml(representer, dumperOptions)

        settingsPath.bufferedWriter().use { it.write(yaml.dump(config)) }
    }
}