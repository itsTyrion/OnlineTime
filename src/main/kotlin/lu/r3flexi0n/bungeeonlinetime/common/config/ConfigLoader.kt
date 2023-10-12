package lu.r3flexi0n.bungeeonlinetime.common.config

import org.slf4j.Logger
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer
import java.nio.file.Path
import kotlin.io.path.*

object ConfigLoader {

    fun load(dataFolderPath: Path, logger: Logger): Config {
        dataFolderPath.createDirectories()
        val settingsPath = dataFolderPath.resolve("settings.yml")
        val yaml = Yaml(BlockRepresenter())

        if (!settingsPath.exists()) {
            settingsPath.createFile()
            save(Config(), yaml, settingsPath)
            logger.warn("No config file was found, default config was created.")
        }
        settingsPath.inputStream().use { input ->
            val config = yaml.loadAs(input, Config::class.java)
            if (config.version_dont_touch < Config.CURRENT_VERSION) {
                config.version_dont_touch = Config.CURRENT_VERSION
                save(config, yaml, settingsPath)
                logger.warn("New config version, please check new values and change if/as needed.")
            }
            return config
        }
    }

    private fun save(config: Config, yaml: Yaml, settingsPath: Path) {
        val dumped = yaml.dump(config)
        settingsPath.bufferedWriter().use { it.write(dumped.substringAfter('\n')) }
    }

    internal class BlockRepresenter : Representer(DumperOptions()) {
        init {
            setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        }
    }
}