package compass_system.wiziplicity.config

import compass_system.wiziplicity.Main
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToStream
import java.lang.Exception
import java.nio.file.Files
import kotlin.io.path.exists

object ConfigHolder {
    private val configPath = getConfigDirectory()?.resolve("${Main.MOD_ID}.json")
    internal var config: ConfigV1 = ConfigV1()
    internal var changed: Boolean = false

    fun load() {
        if (configPath == null) {
            Main.logger.error("Failed to load config due to config directory being missing and not having permission to create it.")
            return
        }

        if (configPath.exists()) {
            val contents = try {
                Files.newBufferedReader(configPath).readText()
            } catch (error: Exception) {
                Main.logger.error("Failed to read config: ", error)
                return backupNewerOrInvalidConfig()
            }

            val schemaVersion = try {
                ignoreKeysJson.decodeFromString<SchemaVersion>(contents).schemaVersion
            } catch (error: Exception) {
                Main.logger.error("""Failed to parse config, cannot read "schema_version": """, error)
                return backupNewerOrInvalidConfig(contents)
            }

            if (schemaVersion == 1) {
                try {
                    config = prettyJson.decodeFromString(contents)
                    changed = false
                } catch (error: Exception) {
                    Main.logger.error("Failed to parse config: ", error)
                    return backupNewerOrInvalidConfig(contents)
                }
            } else {
                Main.logger.error("Invalid schema version: $schemaVersion.")
                return backupNewerOrInvalidConfig(contents)
            }
        }
    }

    private fun backupNewerOrInvalidConfig(contents: String? = null) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val newFilePath = configPath!!.resolveSibling("${Main.MOD_ID}-${now.date}-${now.time.hour}-${now.time.minute}-${now.time.second}.bak.json")

        try {
            Files.move(configPath, newFilePath)
            Main.logger.info("""Renaming invalid "${Main.MOD_ID}.json" to "${newFilePath.fileName}".""")
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
        if (changed || force) {
            changed = false

            try {
                if (configPath == null) {
                    Main.logger.error("Failed to save config due to missing config directory.")
                    Main.logger.error("Config file contents:")
                    Main.logger.error(prettyJson.encodeToString(config))
                    return
                }

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

    fun preserveLastFronter(value: Boolean) {
        if (value != config.preserveLastFronter) {
            config.preserveLastFronter = value

            changed = true
        }
    }

    fun skinChangeDelay(value: Int) {
        if (value != config.skinChangeDelay) {
            config.skinChangeDelay = value

            changed = true
        }
    }

    fun getSkinChangeDelay(): Int {
        val server = Main.currentServer()

        val serverEntry = if (config.aliasedServerSettings.containsKey(server)) {
            config.serverSettings[config.aliasedServerSettings[server]]
        } else {
            config.serverSettings[server]
        }

        return serverEntry?.skinChangeDelay ?: config.skinChangeDelay
    }

    // todo: overhaul with options e.g. do you want to edit / create copy of alias
    fun configureServer(
            address: String = Main.currentServer(),
            action: ServerSettings.() -> Unit
    ): String {
        val existing = config.serverSettings[address]

        if (existing != null) {
            action.invoke(existing)
        } else {
            val alias = config.aliasedServerSettings[address]

            val server = if (alias != null) {
                config.aliasedServerSettings.remove(address)

                config.serverSettings[alias]!!.copy()
            } else {
                ServerSettings()
            }

            action.invoke(server)
            config.serverSettings[address] = server
        }

        changed = true

        return address
    }

    // todo: add option not to delete original settings
    fun createAlias(
            from: String = Main.currentServer(),
            to: String
    ): String {
        if (config.serverSettings.containsKey(from)) {
            config.serverSettings.remove(from)
        }

        config.aliasedServerSettings[from] = to

        return from
    }

    fun getTokens(format: String, allowPronouns: Boolean): NicknameTokens {
        val regex = "\\{([A-z]+)\\}".toRegex()

        val results = regex.findAll(format)

        val validTokens = mutableListOf<String>()
        val invalidTokens = mutableListOf<String>()

        for (result in results) {
            val value = result.groups[1]!!.value

            if (isValidNicknameToken(value, allowPronouns)) {
                validTokens.add(value)
            } else {
                invalidTokens.add(value)
            }
        }

        return NicknameTokens(validTokens, invalidTokens)
    }

    private val validNicknameTokens = setOf("colour", "color", "name", "pronouns")

    private fun isValidNicknameToken(token: String, allowPronouns: Boolean): Boolean {
        return if (token == "pronouns") {
            allowPronouns
        } else {
            token in validNicknameTokens
        }
    }

    fun setNickNameFormatWithPronouns(format: String) {
        if (config.nicknameFormatWithPronouns != format) {
            config.nicknameFormatWithPronouns = format

            changed = true
        }
    }

    fun setNickNameFormatNoPronouns(format: String) {
        if (config.nicknameFormatNoPronouns != format) {
            config.nicknameFormatNoPronouns = format

            changed = true
        }
    }

    fun createHeadmate(id: String): Boolean {
        return if (config.headmates.containsKey(id)) {
            false
        } else {
            config.headmates[id] = Headmate()
            changed = true

            true
        }
    }

    fun renameHeadmate(oldId: String, newId: String): Boolean {
        return if (config.headmates.containsKey(newId)) {
            false
        } else {
            config.headmates[newId] = config.headmates.remove(oldId)!!
            changed = true

            true
        }
    }

    fun deleteHeadmate(id: String) {
        config.headmates.remove(id)
        changed = true
    }
}
