package compass_system.wiziplicity.config

import compass_system.wiziplicity.Main
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import net.fabricmc.loader.api.FabricLoader
import java.lang.Exception
import java.nio.file.Files
import kotlin.io.path.exists

object ConfigHolder {
    private val configPath = FabricLoader.getInstance().configDir.resolve("wiziplicity.json")
    internal var config: ConfigV1? = null
    internal var changed: Boolean = false

    private val ignoreKeysJson = Json { ignoreUnknownKeys = true }
    private val prettyJson = Json { prettyPrint = true }

    fun load() {
        changed = false

        if (configPath.exists()) {
            val contents = try {
                Files.newBufferedReader(configPath).readText()
            }
            catch (error: Exception) {
                Main.logger.error("Failed to read config: ", error)
                config = ConfigV1()
                return backupNewerOrInvalidConfig()
            }

            val schemaVersion = try {
                ignoreKeysJson.decodeFromString<SchemaVersion>(contents).schemaVersion
            } catch (error: Exception) {
                Main.logger.error("""Failed to parse config, cannot read "schema_version": """, error)
                config = ConfigV1()
                return backupNewerOrInvalidConfig(contents)
            }

            if (schemaVersion == 1) {
                config = try {
                    prettyJson.decodeFromString(contents)
                } catch (error: Exception) {
                    Main.logger.error("Failed to parse config: ", error)
                    config = ConfigV1()
                    return backupNewerOrInvalidConfig(contents)
                }
            } else {
                Main.logger.error("Invalid schema version: $schemaVersion.")
                config = ConfigV1()
                return backupNewerOrInvalidConfig(contents)
            }
        } else {
            config = ConfigV1()
        }
    }

    private fun backupNewerOrInvalidConfig(contents: String? = null) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val newFilePath = FabricLoader.getInstance().configDir.resolve("wiziplicity-${now.date}-${now.time.hour}-${now.time.minute}-${now.time.second}.bak.json")

        try {
            Files.move(configPath, newFilePath)
            Main.logger.info("""Renaming invalid "wiziplicity.json" to "${newFilePath.fileName}".""")
        } catch (_: Exception) {
            if (contents != null) {
                Main.logger.error("Failed to backup invalid config, it may be replaced, original file contents: ")
                Main.logger.error(contents)
            } else {
                Main.logger.error("Failed to backup invalid config, it may be replaced.")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(force: Boolean = false) {
        if (changed) {
            config?.also { config ->
                try {
                    Files.newOutputStream(configPath).use {
                        prettyJson.encodeToStream(config, it)
                    }
                } catch (error: Exception) {
                    Main.logger.error("Failed to save config: ", error)
                    Main.logger.error("Config file contents:")
                    Main.logger.error(prettyJson.encodeToString(config))
                }
            }
        }
    }
}