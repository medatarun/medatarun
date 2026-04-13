package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL

/**
 * Event payload used by import/copy to persist the full aggregate content
 * without keeping ModelAggregate inside ModelRepoCmd.
 *
 * Tags are intentionally excluded because storeModelAggregate does not persist
 * them as part of this contract.
 */
@Serializable
data class StoreModelAggregateModel(
    @Contextual
    @SerialName("id")
    val id: ModelId,
    @Contextual
    @SerialName("key")
    val key: ModelKey,
    @Contextual
    @SerialName("name")
    val name: LocalizedText?,
    @Contextual
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @Contextual
    @SerialName("version")
    val version: ModelVersion,
    @Contextual
    @SerialName("origin")
    val origin: ModelOrigin,
    @Contextual
    @SerialName("authority")
    val authority: ModelAuthority,
    @Contextual
    @SerialName("documentationHome")
    val documentationHome: URL?
)

@Serializable
data class StoreModelAggregateType(
    @Contextual
    @SerialName("id")
    val id: TypeId,
    @Contextual
    @SerialName("key")
    val key: TypeKey,
    @Contextual
    @SerialName("name")
    val name: LocalizedText?,
    @Contextual
    @SerialName("description")
    val description: LocalizedMarkdown?
)

@Serializable
data class StoreModelAggregateEntityDeprecated(
    @Contextual
    @SerialName("id")
    val id: EntityId,
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
    @SerialName("identifierAttributeId")
    val identifierAttributeId: AttributeId,
    @Contextual
    @SerialName("origin")
    val origin: EntityOrigin,
    @Contextual
    @SerialName("documentationHome")
    val documentationHome: URL?
)

@Serializable
data class StoreModelAggregateEntityCurrent(
    @Contextual
    @SerialName("id")
    val id: EntityId,
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
    @SerialName("origin")
    val origin: EntityOrigin,
    @Contextual
    @SerialName("documentationHome")
    val documentationHome: URL?
)

@Serializable
data class StoreModelAggregateEntityAttribute(
    @Contextual
    @SerialName("id")
    val id: AttributeId,
    @Contextual
    @SerialName("entityId")
    val entityId: EntityId,
    @Contextual
    @SerialName("key")
    val key: AttributeKey,
    @Contextual
    @SerialName("name")
    val name: LocalizedText?,
    @Contextual
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @Contextual
    @SerialName("typeId")
    val typeId: TypeId,
    @SerialName("optional")
    val optional: Boolean
)

@Serializable
data class StoreModelAggregateRelationship(
    @Contextual
    @SerialName("id")
    val id: RelationshipId,
    @Contextual
    @SerialName("key")
    val key: RelationshipKey,
    @Contextual
    @SerialName("name")
    val name: LocalizedText?,
    @Contextual
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @SerialName("roles")
    val roles: List<StoreModelAggregateRelationshipRole>
)

@Serializable
data class StoreModelAggregateRelationshipRole(
    @Contextual
    @SerialName("id")
    val id: RelationshipRoleId,
    @Contextual
    @SerialName("key")
    val key: RelationshipRoleKey,
    @Contextual
    @SerialName("entityId")
    val entityId: EntityId,
    @Contextual
    @SerialName("name")
    val name: LocalizedText?,
    @Contextual
    @SerialName("cardinality")
    val cardinality: RelationshipCardinality
)

@Serializable
data class StoreModelAggregateRelationshipAttribute(
    @Contextual
    @SerialName("id")
    val id: AttributeId,
    @Contextual
    @SerialName("relationshipId")
    val relationshipId: RelationshipId,
    @Contextual
    @SerialName("key")
    val key: AttributeKey,
    @Contextual
    @SerialName("name")
    val name: LocalizedText?,
    @Contextual
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @Contextual
    @SerialName("typeId")
    val typeId: TypeId,
    @SerialName("optional")
    val optional: Boolean
)

@Serializable
data class StoreModelAggregatePrimaryKey(
    @Contextual
    @SerialName("entityId")
    val entityId: EntityId,
    @Contextual
    @SerialName("participants")
    val participants: List<@Contextual AttributeId>,
)

@Serializable
data class StoreModelAggregateBusinessKey(
    @Contextual
    @SerialName("businessKeyId")
    val businessKeyId: BusinessKeyId,
    @Contextual
    @SerialName("entityId")
    val entityId: EntityId,
    @Contextual
    @SerialName("key")
    val key: BusinessKeyKey,
    @Contextual
    @SerialName("name")
    val name: String?,
    @Contextual
    @SerialName("description")
    val description: String?,
    @Contextual
    @SerialName("participants")
    val participants: List<@Contextual AttributeId>,
)

/**
 * Converts a domain aggregate into the event payload contract stored by
 * ModelRepoCmd.StoreModelAggregate.
 */
object StoreModelAggregatePayloadFactory {
    fun create(modelAggregate: ModelAggregate): ModelStorageCmd.StoreModelAggregate {
        return ModelStorageCmd.StoreModelAggregate(
            model = StoreModelAggregateModel(
                id = modelAggregate.id,
                key = modelAggregate.key,
                name = modelAggregate.name,
                description = modelAggregate.description,
                version = modelAggregate.version,
                origin = modelAggregate.origin,
                authority = modelAggregate.authority,
                documentationHome = modelAggregate.documentationHome,
            ),
            types = modelAggregate.types.map { type ->
                StoreModelAggregateType(
                    id = type.id,
                    key = type.key,
                    name = type.name,
                    description = type.description
                )
            },
            entities = modelAggregate.entities.map { entity ->
                StoreModelAggregateEntityCurrent(
                    id = entity.id,
                    key = entity.key,
                    name = entity.name,
                    description = entity.description,
                    origin = entity.origin,
                    documentationHome = entity.documentationHome
                )
            },
            entityAttributes = modelAggregate.attributes
                .filter { attribute -> attribute.ownerId is AttributeOwnerId.OwnerEntityId }
                .map { attribute ->
                    val ownerId = attribute.ownerId as AttributeOwnerId.OwnerEntityId
                    StoreModelAggregateEntityAttribute(
                        id = attribute.id,
                        entityId = ownerId.id,
                        key = attribute.key,
                        name = attribute.name,
                        description = attribute.description,
                        typeId = attribute.typeId,
                        optional = attribute.optional
                    )
                },
            relationships = modelAggregate.relationships.map { relationship ->
                StoreModelAggregateRelationship(
                    id = relationship.id,
                    key = relationship.key,
                    name = relationship.name,
                    description = relationship.description,
                    roles = relationship.roles.map { role ->
                        StoreModelAggregateRelationshipRole(
                            id = role.id,
                            key = role.key,
                            entityId = role.entityId,
                            name = role.name,
                            cardinality = role.cardinality
                        )
                    }
                )
            },
            relationshipAttributes = modelAggregate.attributes
                .filter { attribute -> attribute.ownerId is AttributeOwnerId.OwnerRelationshipId }
                .map { attribute ->
                    val ownerId = attribute.ownerId as AttributeOwnerId.OwnerRelationshipId
                    StoreModelAggregateRelationshipAttribute(
                        id = attribute.id,
                        relationshipId = ownerId.id,
                        key = attribute.key,
                        name = attribute.name,
                        description = attribute.description,
                        typeId = attribute.typeId,
                        optional = attribute.optional
                    )
                },
            businessKeys = modelAggregate.businessKeys.map {
                StoreModelAggregateBusinessKey(
                    businessKeyId = it.id,
                    entityId = it.entityId,
                    key = it.key,
                    name = it.name,
                    description = it.description,
                    participants = it.participants.sortedBy { p -> p.position }.map { p ->p.attributeId })
            },
            entityPrimaryKeys = modelAggregate.entityPrimaryKeys.map {
                StoreModelAggregatePrimaryKey(
                    entityId = it.entityId,
                    participants = it.participants.sortedBy { p -> p.position }.map { p ->p.attributeId })
            }
        )
    }
}
