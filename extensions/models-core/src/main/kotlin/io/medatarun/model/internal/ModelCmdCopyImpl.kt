package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*

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
    fun copy(model: Model, modelNewKey: ModelKey): Model {
        val typeIds = IdConv("type") { TypeId.generate() }
        val entityIds = IdConv("entity") { EntityId.generate() }
        val attributeIds = IdConv("attribute") { AttributeId.generate() }

        // Respect the order otherwise id conversion will fail!

        val newTypes = model.types.map { type ->
            ModelTypeInMemory.of(type).copy(id = typeIds.generate(type.id))
        }
        val newEntities = model.entityDefs.map { entity ->
            val entityAttributes = entity.attributes.map { attr ->
                AttributeDefInMemory.of(attr).copy(
                    id = attributeIds.generate(attr.id),
                    typeId = typeIds.convert(attr.typeId),
                )
            }
            EntityDefInMemory.of(entity).copy(
                id = entityIds.generate(entity.id),
                identifierAttributeId = attributeIds.convert(entity.identifierAttributeId),
                attributes = entityAttributes
            )
        }
        val newRelationships = model.relationshipDefs.map { rel ->
            RelationshipDefInMemory.of(rel).copy(
                id = rel.id,
                attributes = rel.attributes.map { attr ->
                    AttributeDefInMemory.of(attr).copy(
                        id = AttributeId.generate(),
                        typeId = typeIds.convert(attr.typeId),
                    )
                },
                roles = rel.roles.map { role ->
                    RelationshipRoleInMemory.of(role).copy(
                        id = RelationshipRoleId.generate(),
                        entityId = entityIds.convert(role.entityId)
                    )
                }
            )
        }
        val next = ModelInMemory.of(model)
            .copy(
                id = ModelId.generate(),
                key = modelNewKey,
                types = newTypes,
                entityDefs = newEntities,
                relationshipDefs = newRelationships
            )
        return next
    }
}