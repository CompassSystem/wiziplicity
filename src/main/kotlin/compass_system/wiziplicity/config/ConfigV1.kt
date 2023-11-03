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
        val headmates: Map<String, Headmate> = mutableMapOf(),
        val serverSettings: Map<String, ServerSettings> = mutableMapOf(),
        val aliasedServerSettings: Map<String, String> = mutableMapOf()
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

        ConfigV1(nicknames["no_pronouns"], nicknames["with_pronouns"], skinChangeDelay, headmates.toMutableMap(), serverSettings.toMutableMap(), aliasedServers.toMutableMap())
    }

}

@Serializable(with = ProxySerializer::class)
data class Proxy(
        val prefix: String? = null,
        val suffix: String? = null
)

class ProxySerializer : KSerializer<Proxy> {
    override val descriptor = PrimitiveSerialDescriptor("compass_system.wiziplicity.config.ProxySerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Proxy {
        val value = decoder.decodeString()

        val left = value.substringBefore("text")

        if (left == value) {
            throw SerializationException("text not found in value")
        }

        if (left.isEmpty()) {
            val right = value.substringAfter("text")
            if (right.isEmpty()) {
                throw SerializationException("no prefix or suffix found")
            } else {
                return Proxy(suffix = right)
            }
        } else {
            val right = value.substringAfter("text")
            if (right.isNotEmpty()) {
                throw SerializationException("both prefix and suffix found, cannot deserialize illegal proxy.")
            } else {
                return Proxy(prefix = left)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Proxy) {
        if (value.prefix != null && value.suffix != null) {
            throw IllegalArgumentException("Both prefix and suffix found, cannot serialize illegal proxy.")
        }else if (value.prefix != null) {
            encoder.encodeString("${value.prefix}text")
        } else if (value.suffix != null) {
            encoder.encodeString("text${value.suffix}")
        }
    }

}

@Serializable
data class Headmate(
        val name: String? = null,
        val nickname: String? = null,
        val pronouns: String? = null,
        val proxytags: List<Proxy> = listOf(),
        val skin: String? = null,
        val color: String? = null
)

@Serializable
data class ServerSettings(
        @SerialName("skin_change_delay")
        val skinChangeDelay: Int
)
