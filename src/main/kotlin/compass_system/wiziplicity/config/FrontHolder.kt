package compass_system.wiziplicity.config

import compass_system.wiziplicity.Main
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Files
import java.nio.file.Path

@Serializable
data class Fronts(
        val fronts: MutableMap<String, String> = mutableMapOf(),
        var lastFronter: String? = null
)

object FrontHolder {
    private val filePath: Path? = getConfigDirectory()?.resolve("fronts.json")
    private var fronts: Fronts = Fronts()
    private var changed: Boolean = false

    @OptIn(ExperimentalSerializationApi::class)
    fun load() {
        if (filePath == null) {
            return
        }

        val stream = try {
            Files.newInputStream(filePath)
        } catch (error: Exception) {
            return Main.logger.error("""Failed to open "fronts.json" for reading: """, error)
        }

        try {
            fronts = prettyJson.decodeFromStream<Fronts>(stream)
            changed = false
        } catch (error: Exception) {
            Main.logger.error("""Failed to deserialize "fronts.json": """, error)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(force: Boolean = false) {
        if (changed || force) {
            if (filePath == null) {
                Main.logger.error("""Failed to save "fronts.json" due to previously failing to create the config directory, file contents:""")
                return Main.logger.error(prettyJson.encodeToString(fronts))
            }

            val stream = try {
                Files.newOutputStream(filePath)
            } catch (error: Exception) {
                Main.logger.error("""Failed to open "fronts.json" for writing: """, error)
                Main.logger.error(""""fronts.json" file contents:""")
                return Main.logger.error(prettyJson.encodeToString(fronts))
            }

            try {
                prettyJson.encodeToStream(fronts, stream)
            } catch (error: java.lang.Exception) {
                Main.logger.error("""Failed to serialize "fronts.json": """, error)
                Main.logger.error(""""fronts.json" file contents:""")
                Main.logger.error(prettyJson.encodeToString(fronts))
            }
        }
    }
}
