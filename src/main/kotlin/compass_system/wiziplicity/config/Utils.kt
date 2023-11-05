package compass_system.wiziplicity.config

import compass_system.wiziplicity.Main
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

@Serializable
data class SchemaVersion(
        @SerialName("schema_version")
        val schemaVersion: Int
)

internal val ignoreKeysJson = Json { ignoreUnknownKeys = true }
internal val prettyJson = Json { prettyPrint = true }

private var configDirectory: Path? = null
private var configInitialized = false

internal fun getConfigDirectory(): Path? {
    return if (configInitialized) {
        configDirectory
    } else {
        val path = FabricLoader.getInstance().configDir.resolve(Main.MOD_ID);

        configInitialized = true

        return if (!path.exists()) {
            try {
                configDirectory = Files.createDirectories(path)
                configDirectory
            } catch (error: Exception) {
                Main.logger.error("Failed to create ${Main.MOD_ID} config directory: ", error)
                null
            }
        } else {
            configDirectory = path
            configDirectory
        }
    }
}

data class NicknameTokens(
        val valid: List<String>,
        val invalid: List<String>
)
