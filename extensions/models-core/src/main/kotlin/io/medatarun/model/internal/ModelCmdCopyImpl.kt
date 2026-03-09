package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.infra.inmemory.ModelInMemory

class ModelCmdCopyImpl {

    class IdConv<T>(val name: String, val factory: () -> T) {
        val map = mutableMapOf<T, T>()
        fun generate(old: T): T {
            val newId = factory()
            map[old] = newId
            return newId
        }

        fun convert(old: T): T {
            return map[old] ?: throw CopyModelIdConversionFailedException(name, old.toString())
        }
    }

    fun copy(model: ModelAggregate, modelNewKey: ModelKey): ModelAggregate {
        val typeIds = IdConv("type") { TypeId.generate() }
        val entityIds = IdConv("entity") { EntityId.generate() }
        val relationshipId = IdConv("relationship") { RelationshipId.generate() }
        val attributeIds = IdConv("attribute") { AttributeId.generate() }

        // Respect the order otherwise id conversion will fail!

        val newTypes = model.types.map { type ->
            ModelTypeInMemory.of(type).copy(id = typeIds.generate(type.id))
        }
        // Build attribute id conversions before copying entities because entities reference identifier attributes.
        model.attributes.forEach { attr ->
            attributeIds.generate(attr.id)
        }
        val newEntities = model.entities.map { entity ->
            EntityInMemory.of(entity).copy(
                id = entityIds.generate(entity.id),
                identifierAttributeId = attributeIds.convert(entity.identifierAttributeId),
            )
        }
        val newRelationships = model.relationships.map { rel ->
            RelationshipInMemory.of(rel).copy(
                id = relationshipId.generate(rel.id),
                roles = rel.roles.map { role ->
                    RelationshipRoleInMemory.of(role).copy(
                        id = RelationshipRoleId.generate(),
                        entityId = entityIds.convert(role.entityId)
                    )
                }
            )
        }

        val attributes = model.attributes.map { attr ->
            val ownerId = attr.ownerId
            AttributeInMemory.of(attr).copy(
                id = attributeIds.convert(attr.id),
                typeId = typeIds.convert(attr.typeId),
                ownerId = when (ownerId) {
                    is AttributeOwnerId.OwnerEntityId -> AttributeOwnerId.OwnerEntityId(entityIds.convert(ownerId.id))
                    is AttributeOwnerId.OwnerRelationshipId -> AttributeOwnerId.OwnerRelationshipId(
                        relationshipId.convert(
                            ownerId.id
                        )
                    )
                }
            )
        }

        val next = ModelAggregateInMemory.of(model)
            .copy(
                model = ModelInMemory.of(model).copy(
                    id = ModelId.generate(),
                    key = modelNewKey,
                    // A model copy falls back to system, always (business rule) to not pollute the list of canonical models. Users can promote that manually later.
                    authority = ModelAuthority.SYSTEM,
                ),
                types = newTypes,
                entities = newEntities,
                relationships = newRelationships,
                attributes = attributes
            )
        return next
    }
}
