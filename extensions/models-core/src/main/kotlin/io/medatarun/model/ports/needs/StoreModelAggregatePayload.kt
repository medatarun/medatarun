@file:UseContextualSerialization(
    AttributeId::class,
    AttributeKey::class,
    EntityId::class,
    EntityKey::class,
    EntityOrigin::class,
    LocalizedMarkdown::class,
    LocalizedText::class,
    ModelAuthority::class,
    ModelId::class,
    ModelKey::class,
    ModelOrigin::class,
    ModelVersion::class,
    RelationshipCardinality::class,
    RelationshipId::class,
    RelationshipKey::class,
    RelationshipRoleId::class,
    RelationshipRoleKey::class,
    TypeId::class,
    TypeKey::class,
    URL::class,
)

package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
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
    @SerialName("id")
    val id: ModelId,
    @SerialName("key")
    val key: ModelKey,
    @SerialName("name")
    val name: LocalizedText?,
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @SerialName("version")
    val version: ModelVersion,
    @SerialName("origin")
    val origin: ModelOrigin,
    @SerialName("authority")
    val authority: ModelAuthority,
    @SerialName("documentation_home")
    val documentationHome: URL?
)

@Serializable
data class StoreModelAggregateType(
    @SerialName("id")
    val id: TypeId,
    @SerialName("key")
    val key: TypeKey,
    @SerialName("name")
    val name: LocalizedText?,
    @SerialName("description")
    val description: LocalizedMarkdown?
)

@Serializable
data class StoreModelAggregateEntity(
    @SerialName("id")
    val id: EntityId,
    @SerialName("key")
    val key: EntityKey,
    @SerialName("name")
    val name: LocalizedText?,
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @SerialName("identifier_attribute_id")
    val identifierAttributeId: AttributeId,
    @SerialName("origin")
    val origin: EntityOrigin,
    @SerialName("documentation_home")
    val documentationHome: URL?
)

@Serializable
data class StoreModelAggregateEntityAttribute(
    @SerialName("id")
    val id: AttributeId,
    @SerialName("entity_id")
    val entityId: EntityId,
    @SerialName("key")
    val key: AttributeKey,
    @SerialName("name")
    val name: LocalizedText?,
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @SerialName("type_id")
    val typeId: TypeId,
    @SerialName("optional")
    val optional: Boolean
)

@Serializable
data class StoreModelAggregateRelationship(
    @SerialName("id")
    val id: RelationshipId,
    @SerialName("key")
    val key: RelationshipKey,
    @SerialName("name")
    val name: LocalizedText?,
    @SerialName("description")
    val description: LocalizedMarkdown?,
    @SerialName("roles")
    val roles: List<StoreModelAggregateRelationshipRole>
)

@Serializable
data class StoreModelAggregateRelationshipRole(
    @SerialName("id")
    val id: RelationshipRoleId,
    @SerialName("key")
    val key: RelationshipRoleKey,
    @SerialName("entity_id")
    val entityId: EntityId,
    @SerialName("name")
    val name: LocalizedText?,
    @SerialName("cardinality")
    val cardinality: RelationshipCardinality
)

@Serializable
data class StoreModelAggregateRelationshipAttribute(
    @SerialName("id")
    val id: AttributeId,
    @SerialName("relationship_id")
    val relationshipId: RelationshipId,
    @SerialName("key")
    val key: AttributeKey,
    @SerialName("name")
    val name: LocalizedText?,
    @SerialName("description")
    val description: LocalizedMarkdown?,
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
