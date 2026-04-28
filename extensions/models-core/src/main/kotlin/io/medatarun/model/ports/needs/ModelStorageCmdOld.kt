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
    @StorageEventContract(eventType = "model_aggregate_stored", eventVersion = 1)
    data class StoreModelAggregate(
        @SerialName("model")
        val model: StoreModelAggregateModel,
        @SerialName("types")
        val types: List<StoreModelAggregateType>,
        @SerialName("entities")
        val entities: List<StoreModelAggregateEntityDeprecated>,
        @SerialName("entityAttributes")
        val entityAttributes: List<StoreModelAggregateEntityAttribute>,
        @SerialName("relationships")
        val relationships: List<StoreModelAggregateRelationship>,
        @SerialName("relationshipAttributes")
        val relationshipAttributes: List<StoreModelAggregateRelationshipAttribute>,
    ) : ModelStorageCmdOld

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
        val name: TextSingleLine?,
        @Contextual
        @SerialName("description")
        val description: TextMarkdown?,
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
        val identityAttributeName: TextSingleLine?,
        @Contextual
        @SerialName("identityAttributeDescription")
        val identityAttributeDescription: TextMarkdown?,
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