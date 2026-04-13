package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import io.medatarun.storage.eventsourcing.StorageEventContract
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
sealed interface ModelStorageCmdOld : ModelStorageCmdAnyVersion {

    @Serializable
    @StorageEventContract(eventType = "entity_created", eventVersion = 1)
    @Deprecated("Use v2")
    data class CreateEntity(
        @Contextual
        @SerialName("modelId")
        val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("key")
        val key: EntityKey,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @Contextual
        @SerialName("documentationHome")
        val documentationHome: URL?,
        @Contextual
        @SerialName("origin")
        val origin: EntityOrigin,
        @Contextual
        @SerialName("identityAttributeId")
        val identityAttributeId: AttributeId,
        @Contextual
        @SerialName("identityAttributeKey")
        val identityAttributeKey: AttributeKey,
        @Contextual
        @SerialName("identityAttributeTypeId")
        val identityAttributeTypeId: TypeId,
        @Contextual
        @SerialName("identityAttributeName")
        val identityAttributeName: LocalizedText?,
        @Contextual
        @SerialName("identityAttributeDescription")
        val identityAttributeDescription: LocalizedMarkdown?,
        @SerialName("identityAttributeOptional")
        val identityAttributeIdOptional: Boolean,

        ) : ModelStorageCmdOld
    @Serializable
    @StorageEventContract(eventType = "entity_identifier_attribute_updated", eventVersion = 1)
    data class UpdateEntityIdentifierAttribute(
        @Contextual
        @SerialName("modelId")
        val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("identifierAttributeId")
        val identifierAttributeId: AttributeId
    ) : ModelStorageCmdOld


}