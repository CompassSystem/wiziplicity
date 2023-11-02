package compass_system.wiziplicity.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SchemaVersion(
        @SerialName("schema_version")
        val schemaVersion: Int
)
