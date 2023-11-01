package compass_system.wiziplicity.config

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable(with = ConfigV1Serializer::class)
data class ConfigV1(
        val nicknameFormatNoPronouns: String? = null,
        val nicknameFormatWithPronouns: String? = null,
        val skinChangeDelay: Int = 60,
        val headmates: Map<String, Headmate> = mapOf(),
        val serverSettings: Map<String, ServerSettings> = mapOf(),
        val aliasedServerSettings: Map<String, String> = mapOf()
)

class ConfigV1Serializer : KSerializer<ConfigV1> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ConfigV0") {
        element<Int>("schema_version")
        element("nickname_format", mapSerialDescriptor<String, String>())
        element<Int>("skin_change_delay")
        element("headmates", mapSerialDescriptor<String, Headmate>())
        element("server_settings", mapSerialDescriptor<String, JsonElement>())
    }

    override fun serialize(encoder: Encoder, value: ConfigV1) {
        val nicknames = buildMap {
            value.nicknameFormatNoPronouns?.let { this["no_pronouns"] = it }
            value.nicknameFormatWithPronouns?.let { this["with_pronouns"] = it }
        }

        val combinedSettingsMap = buildMap {
            value.serverSettings.forEach { (id, settings) ->
                this[id] = JsonObject(mapOf("skin_change_delay" to JsonPrimitive(settings.skinChangeDelay)))
            }

            value.aliasedServerSettings.forEach { (alias, original) ->
                this[alias] = JsonPrimitive(original)
            }
        }

        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, 1)
            encodeSerializableElement(descriptor, 1, serializer<Map<String, String>>(), nicknames)
            encodeIntElement(descriptor, 2, value.skinChangeDelay)
            encodeSerializableElement(descriptor, 3, serializer<Map<String, Headmate>>(), value.headmates)
            encodeSerializableElement(descriptor, 4, serializer<Map<String, JsonElement>>(), combinedSettingsMap)
        }
    }

    override fun deserialize(decoder: Decoder): ConfigV1 = decoder.decodeStructure(descriptor) {
        var nicknames: Map<String, String> = emptyMap()
        var skinChangeDelay: Int = 60
        var headmates: Map<String, Headmate> = emptyMap()
        var combinedSettings: Map<String, JsonElement> = emptyMap()

        loop@ while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                DECODE_DONE -> break@loop

                0 -> {
                    val schemaVersion = decodeIntElement(descriptor, 0)

                    if (schemaVersion != 1) {
                        throw SerializationException("Expected schema_version = 1, found: $schemaVersion")
                    }
                }
                1 -> nicknames = decodeSerializableElement(descriptor, 1, serializer<Map<String, String>>())
                2 -> skinChangeDelay = decodeIntElement(descriptor, 2)
                3 -> headmates = decodeSerializableElement(descriptor, 3, serializer<Map<String, Headmate>>())
                4 -> combinedSettings = decodeSerializableElement(descriptor, 4, serializer<Map<String, JsonElement>>())

                else -> throw SerializationException("Unexpected index $index")
            }
        }

        val serverSettings = combinedSettings.filterValues { it is JsonObject }.mapValues { Json.decodeFromJsonElement(ServerSettings.serializer(), it.value as JsonObject) }
        val aliasedServers = combinedSettings.filterValues { it is JsonPrimitive }.mapValues { (it.value as JsonPrimitive).content }

        ConfigV1(nicknames["no_pronouns"]!!, nicknames["with_pronouns"], skinChangeDelay, headmates, serverSettings, aliasedServers)
    }

}

@Serializable
data class Headmate(
        val name: String? = null,
        val nickname: String? = null,
        val pronouns: String? = null,
        val proxytags: List<String> = listOf(),
        val skin: String? = null,
        val color: String? = null
)

@Serializable
data class ServerSettings(
        @SerialName("skin_change_delay")
        val skinChangeDelay: Int
)
