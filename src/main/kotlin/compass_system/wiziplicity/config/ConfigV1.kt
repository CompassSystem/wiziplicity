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

// todo replace with/no pronouns with a separate pronouns format which changes {pronouns}
@Serializable(with = ConfigV1Serializer::class)
data class ConfigV1(
        var nicknameFormatNoPronouns: String? = null,
        var nicknameFormatWithPronouns: String? = null,
        var skinChangeDelay: Int = 60,
        var preserveLastFronter: Boolean = true,
        var caseSensitiveProxies: Boolean = true,
        val headmates: MutableMap<String, Headmate> = mutableMapOf(),
        val serverSettings: MutableMap<String, ServerSettings> = mutableMapOf(),
        val aliasedServerSettings: MutableMap<String, String> = mutableMapOf()
)

class ConfigV1Serializer : KSerializer<ConfigV1> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ConfigV1") {
        element<Int>("schema_version")
        element("nickname_format", mapSerialDescriptor<String, String>())
        element<Int>("skin_change_delay")
        element<Boolean>("preserve_last_fronter")
        element<Boolean>("case_sensitive_proxies")
        element("headmates", mapSerialDescriptor<String, ProtoHeadmate>())
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
            encodeBooleanElement(descriptor, 4, value.caseSensitiveProxies)
            encodeSerializableElement(descriptor, 5, serializer<Map<String, ProtoHeadmate>>(), value.headmates.mapValues { it.value.toSerializable() })
            encodeSerializableElement(descriptor, 6, serializer<Map<String, JsonElement>>(), combinedSettingsMap)
        }
    }

    @Suppress("RedundantExplicitType")
    override fun deserialize(decoder: Decoder): ConfigV1 = decoder.decodeStructure(descriptor) {
        var nicknames: Map<String, String> = emptyMap()
        var skinChangeDelay: Int = 60
        var preserveLastFronter = true
        var caseSensitiveProxies = true
        var headmates: Map<String, ProtoHeadmate> = emptyMap()
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
                4 -> caseSensitiveProxies = decodeBooleanElement(descriptor, 3)
                5 -> headmates = decodeSerializableElement(descriptor, 4, serializer<Map<String, ProtoHeadmate>>())
                6 -> combinedSettings = decodeSerializableElement(descriptor, 5, serializer<Map<String, JsonElement>>())

                else -> throw SerializationException("Unexpected index $index")
            }
        }

        val serverSettings = combinedSettings.filterValues { it is JsonObject }.mapValues { prettyJson.decodeFromJsonElement(ProtoServerSettings.serializer(), it.value as JsonObject) }
        val aliasedServers = combinedSettings.filterValues { it is JsonPrimitive }.mapValues { (it.value as JsonPrimitive).content }

        ConfigV1(
                nicknameFormatNoPronouns = nicknames["no_pronouns"],
                nicknameFormatWithPronouns = nicknames["with_pronouns"],
                skinChangeDelay = skinChangeDelay,
                preserveLastFronter = preserveLastFronter,
                caseSensitiveProxies = caseSensitiveProxies,
                headmates = headmates.mapValues { it.value.construct(it.key) }.toMutableMap(),
                serverSettings = serverSettings.mapValues { it.value.construct(it.key) }.toMutableMap(),
                aliasedServerSettings = aliasedServers.toMutableMap()
        )
    }

}

enum class FixPosition {
    START,
    END
}

@Serializable(with = ProxySerializer::class)
data class Proxy(
        val fix: String,
        val fixPosition: FixPosition
) {
    override fun toString(): String {
        return if (fixPosition == FixPosition.START) {
            "${fix}text"
        } else {
            "text${fix}"
        }
    }

    companion object {
        fun of(value: String, errorCreator: (String) -> Exception): Proxy {
            if ("text" !in value) {
                throw errorCreator("Cannot find \"text\" in value, proxy must take format \"<prefix>text\" or \"text<suffix>\".")
            }

            val left = value.substringBefore("text")
            val right = value.substringAfter("text")

            if (left.isNotEmpty() && right.isNotEmpty()) {
                throw errorCreator("Cannot create a proxy with both a prefix and a suffix.")
            }

            if (left.isEmpty() && right.isEmpty()) {
                throw errorCreator("Cannot create a proxy without a prefix or a suffix.")
            }

            return if (right.isEmpty()) {
                Proxy(left, FixPosition.START)
            } else {
                Proxy(right, FixPosition.END)
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

    override fun serialize(encoder: Encoder, value: Proxy) = encoder.encodeString(value.toString())
}

@Serializable
data class ProtoHeadmate(
        var name: String? = null,
        var pronouns: String? = null,
        val proxytags: MutableList<Proxy> = mutableListOf(),
        var skin: String? = null,
        @SerialName("skin_type")
        var skinType: String? = null,
        var color: String? = null
) {
    fun construct(id: String) = Headmate(id, name, pronouns, proxytags, skin, skinType, color)
}

data class Headmate(
        val id: String,

        var name: String? = null,
        var pronouns: String? = null,
        val proxytags: MutableList<Proxy> = mutableListOf(),
        var skin: String? = null,
        var skinType: String? = null,
        var color: String? = null
) {
    fun toSerializable() = ProtoHeadmate(name, pronouns, proxytags,skin, skinType, color)

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
        // fix for: https://github.com/Patbox/TextPlaceholderAPI/issues/44
        val color = (this.color ?: "white").let {
            when (it) {
                "orange" -> "gold"
                "pink" -> "light_purple"
                "grey" -> "gray"
                "dark_grey" -> "dark_gray"
                else -> it
            }
        }

        val name = name ?: id
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
data class ProtoServerSettings(
        @SerialName("skin_change_delay")
        var skinChangeDelay: Int = 60
) {
    fun construct(id: String) = ServerSettings(id, skinChangeDelay)
}

data class ServerSettings(
        val id: String,
        var skinChangeDelay: Int = 60
)
