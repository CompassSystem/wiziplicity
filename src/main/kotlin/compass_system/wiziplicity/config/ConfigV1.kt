package compass_system.wiziplicity.config

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.lang.IllegalStateException

// todo replace with/no pronouns with a separate pronouns format which changes {pronouns}
@Serializable(with = ConfigV1Serializer::class)
data class ConfigV1(
        var nicknameFormatNoPronouns: String? = null,
        var nicknameFormatWithPronouns: String? = null,
        var skinChangeDelay: Int = 60,
        var preserveLastFronter: Boolean = true,
        val headmates: MutableMap<String, Headmate> = mutableMapOf(),
        val serverSettings: MutableMap<String, ServerSettings> = mutableMapOf(),
        val aliasedServerSettings: MutableMap<String, String> = mutableMapOf()
)

class ConfigV1Serializer : KSerializer<ConfigV1> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ConfigV0") {
        element<Int>("schema_version")
        element("nickname_format", mapSerialDescriptor<String, String>())
        element<Int>("skin_change_delay")
        element<Boolean>("preserve_last_fronter")
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
            encodeBooleanElement(descriptor, 3, value.preserveLastFronter)
            encodeSerializableElement(descriptor, 4, serializer<Map<String, Headmate>>(), value.headmates)
            encodeSerializableElement(descriptor, 5, serializer<Map<String, JsonElement>>(), combinedSettingsMap)
        }
    }

    override fun deserialize(decoder: Decoder): ConfigV1 = decoder.decodeStructure(descriptor) {
        var nicknames: Map<String, String> = emptyMap()
        var skinChangeDelay: Int = 60
        var preserveLastFronter = true
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
                3 -> preserveLastFronter = decodeBooleanElement(descriptor, 3)
                4 -> headmates = decodeSerializableElement(descriptor, 4, serializer<Map<String, Headmate>>())
                5 -> combinedSettings = decodeSerializableElement(descriptor, 5, serializer<Map<String, JsonElement>>())

                else -> throw SerializationException("Unexpected index $index")
            }
        }

        val serverSettings = combinedSettings.filterValues { it is JsonObject }.mapValues { prettyJson.decodeFromJsonElement(ServerSettings.serializer(), it.value as JsonObject) }
        val aliasedServers = combinedSettings.filterValues { it is JsonPrimitive }.mapValues { (it.value as JsonPrimitive).content }

        ConfigV1(
                nicknameFormatNoPronouns = nicknames["no_pronouns"],
                nicknameFormatWithPronouns = nicknames["with_pronouns"],
                skinChangeDelay = skinChangeDelay,
                preserveLastFronter = preserveLastFronter,
                headmates = headmates.toMutableMap(),
                serverSettings = serverSettings.toMutableMap(),
                aliasedServerSettings = aliasedServers.toMutableMap()
        )
    }

}

@Serializable(with = ProxySerializer::class)
data class Proxy(
        val prefix: String? = null,
        val suffix: String? = null
) {
    override fun toString(): String {
        return if (prefix != null && suffix != null) {
            throw IllegalStateException("Illegal proxy")
        } else if (prefix != null) {
            "${prefix}text"
        } else {
            "text${suffix}"
        }
    }

    companion object {
        fun of(value: String, errorCreator: (String) -> Exception): Proxy {
            val left = value.substringBefore("text")

            if (left == value) {
                throw errorCreator("text not found in value")
            }

            if (left.isEmpty()) {
                val right = value.substringAfter("text")
                if (right.isEmpty()) {
                    throw errorCreator("no prefix or suffix found")
                } else {
                    return Proxy(suffix = right)
                }
            } else {
                val right = value.substringAfter("text")
                if (right.isNotEmpty()) {
                    throw errorCreator("both prefix and suffix found, cannot decode illegal proxy.")
                } else {
                    return Proxy(prefix = left)
                }
            }
        }
    }
}

class ProxySerializer : KSerializer<Proxy> {
    override val descriptor = PrimitiveSerialDescriptor("compass_system.wiziplicity.config.ProxySerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Proxy {
        val value = decoder.decodeString()

        return Proxy.of(value, errorCreator = ::SerializationException)
    }

    override fun serialize(encoder: Encoder, value: Proxy) {
        if (value.prefix != null && value.suffix != null) {
            throw SerializationException("Both prefix and suffix found, cannot serialize illegal proxy.")
        }else if (value.prefix != null) {
            encoder.encodeString("${value.prefix}text")
        } else if (value.suffix != null) {
            encoder.encodeString("text${value.suffix}")
        }
    }

}

@Serializable
data class Headmate(
        var name: String? = null,
        var nickname: String? = null,
        var pronouns: String? = null,
        val proxytags: MutableList<Proxy> = mutableListOf(),
        var skin: String? = null,
        var color: String? = null
) {
    fun addProxy(proxyObj: Proxy) = if (proxyObj in proxytags) {
        false
    } else {
        proxytags.add(proxyObj)

        true
    }

    fun removeProxy(proxyObj: Proxy) = if (proxyObj !in proxytags) {
        false
    } else {
        proxytags.remove(proxyObj)

        true
    }

    fun setProxy(proxyObj: Proxy) {
        proxytags.clear()
        proxytags.add(proxyObj)
    }

    fun getStyledNickname(): String {
        val color = this.color ?: "white"
        val name = nickname ?: ConfigHolder.config.headmates.filter { it.value == this }.keys.first()
        val pronouns = pronouns

        return if (pronouns == null) {
            ConfigHolder.config.nicknameFormatNoPronouns!!
                    .replace("{color}", color)
                    .replace("{colour}", color)
                    .replace("{name}", name)
        } else {
            ConfigHolder.config.nicknameFormatWithPronouns!!
                    .replace("{pronouns}", pronouns)
                    .replace("{color}", color)
                    .replace("{colour}", color)
                    .replace("{name}", name)
        }
    }
}

@Serializable
data class ServerSettings(
        @SerialName("skin_change_delay")
        var skinChangeDelay: Int = 60
)
