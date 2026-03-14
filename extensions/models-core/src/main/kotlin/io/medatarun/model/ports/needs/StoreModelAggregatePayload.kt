package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import java.net.URL

/**
 * Event payload used by import/copy to persist the full aggregate content
 * without keeping ModelAggregate inside ModelRepoCmd.
 *
 * Tags are intentionally excluded because storeModelAggregate does not persist
 * them as part of this contract.
 */
data class StoreModelAggregateModel(
    val id: ModelId,
    val key: ModelKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val version: ModelVersion,
    val origin: ModelOrigin,
    val authority: ModelAuthority,
    val documentationHome: URL?
)

data class StoreModelAggregateType(
    val id: TypeId,
    val key: TypeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)

data class StoreModelAggregateEntity(
    val id: EntityId,
    val key: EntityKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identifierAttributeId: AttributeId,
    val origin: EntityOrigin,
    val documentationHome: URL?
)

data class StoreModelAggregateEntityAttribute(
    val id: AttributeId,
    val entityId: EntityId,
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeId: TypeId,
    val optional: Boolean
)

data class StoreModelAggregateRelationship(
    val id: RelationshipId,
    val key: RelationshipKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val roles: List<StoreModelAggregateRelationshipRole>
)

data class StoreModelAggregateRelationshipRole(
    val id: RelationshipRoleId,
    val key: RelationshipRoleKey,
    val entityId: EntityId,
    val name: LocalizedText?,
    val cardinality: RelationshipCardinality
)

data class StoreModelAggregateRelationshipAttribute(
    val id: AttributeId,
    val relationshipId: RelationshipId,
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeId: TypeId,
    val optional: Boolean
)

/**
 * Converts a domain aggregate into the event payload contract stored by
 * ModelRepoCmd.StoreModelAggregate.
 */
object StoreModelAggregatePayloadFactory {
    fun create(modelAggregate: ModelAggregate): ModelRepoCmd.StoreModelAggregate {
        return ModelRepoCmd.StoreModelAggregate(
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
