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
    @SerialName("documentation_home")
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
data class StoreModelAggregateEntity(
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
    @SerialName("identifier_attribute_id")
    val identifierAttributeId: AttributeId,
    @Contextual
    @SerialName("origin")
    val origin: EntityOrigin,
    @Contextual
    @SerialName("documentation_home")
    val documentationHome: URL?
)

@Serializable
data class StoreModelAggregateEntityAttribute(
    @Contextual
    @SerialName("id")
    val id: AttributeId,
    @Contextual
    @SerialName("entity_id")
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
    @SerialName("type_id")
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
    @SerialName("entity_id")
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
    @SerialName("relationship_id")
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
    @SerialName("type_id")
    val typeId: TypeId,
    @SerialName("optional")
    val optional: Boolean
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
                StoreModelAggregateEntity(
                    id = entity.id,
                    key = entity.key,
                    name = entity.name,
                    description = entity.description,
                    identifierAttributeId = entity.identifierAttributeId,
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
                }
        )
    }
}
