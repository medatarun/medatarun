package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelTypeUpdateCmd
import io.medatarun.model.ports.needs.*
import io.medatarun.tags.core.domain.TagId

class ModelInMemoryReducer {
    fun dispatch(model: ModelInMemory, cmd: ModelRepoCmdOnModel): ModelInMemory {
        return when (cmd) {


            is ModelRepoCmd.UpdateModelDescription -> model.copy(description = cmd.description)
            is ModelRepoCmd.UpdateModelName -> model.copy(name = cmd.name)
            is ModelRepoCmd.UpdateModelVersion -> model.copy(version = cmd.version)
            is ModelRepoCmd.UpdateModelDocumentationHome -> model.copy(documentationHome = cmd.url)
            is ModelRepoCmd.UpdateModelTagAdd -> model.copy(
                tags = tagAdd(
                    model.tags,
                    cmd.tagId
                )
            )

            is ModelRepoCmd.UpdateModelTagDelete -> model.copy(
                tags = tagDelete(
                    model.tags,
                    cmd.tagId
                )
            )

            is ModelRepoCmd.DeleteModel -> throw ModelInMemoryReducerCommandNotSupportedException(cmd)

            is ModelRepoCmd.CreateType -> model.copy(
                types = model.types + ModelTypeInMemory(
                    id = TypeId.generate(),
                    key = cmd.initializer.id,
                    name = cmd.initializer.name,
                    description = cmd.initializer.description
                )
            )

            is ModelRepoCmd.UpdateType -> model.copy(types = model.types.map { type ->
                if (type.id != cmd.typeId) type else when (cmd.cmd) {
                    is ModelTypeUpdateCmd.Name -> type.copy(name = cmd.cmd.value)
                    is ModelTypeUpdateCmd.Description -> type.copy(description = cmd.cmd.value)
                    is ModelTypeUpdateCmd.Key -> type.copy(key = cmd.cmd.value)
                }
            })

            is ModelRepoCmd.DeleteType -> model.copy(types = model.types.mapNotNull { type -> if (type.id != cmd.typeId) type else null })

            is ModelRepoCmd.CreateEntity -> model.copy(entities = model.entities + EntityInMemory.of(cmd.entity))
            is ModelRepoCmd.UpdateEntity -> modifyingEntity(model, cmd.entityId) { previous ->
                when (val c = cmd.cmd) {
                    is ModelRepoCmdEntityUpdate.Key -> previous.copy(key = c.value)
                    is ModelRepoCmdEntityUpdate.Name -> previous.copy(name = c.value)
                    is ModelRepoCmdEntityUpdate.Description -> previous.copy(description = c.value)
                    is ModelRepoCmdEntityUpdate.IdentifierAttribute -> {
                        val entityRef = EntityRef.ById(cmd.entityId)
                        val attr = model.findEntityAttributeOptional(entityRef, cmd.cmd.value)
                            ?: throw EntityAttributeNotFoundException(ModelRef.ById(model.id), entityRef, EntityAttributeRef.ById(cmd.cmd.value))
                        previous.copy(identifierAttributeId = attr.id)
                    }
                    is ModelRepoCmdEntityUpdate.DocumentationHome -> previous.copy(documentationHome = c.value)
                }
            }

            is ModelRepoCmd.UpdateEntityTagAdd ->
                modifyingEntity(model, cmd.entityId) { previous ->
                    previous.copy(tags = tagAdd(previous.tags, cmd.tagId))
                }

            is ModelRepoCmd.UpdateEntityTagDelete ->
                modifyingEntity(model, cmd.entityId) { previous ->
                    previous.copy(tags = tagDelete(previous.tags, cmd.tagId))
                }

            is ModelRepoCmd.DeleteEntity -> modifyingEntity(model, cmd.entityId) { null }

            is ModelRepoCmd.CreateEntityAttribute -> modifyingEntity(model, cmd.entityId) {
                it.copy(attributes = it.attributes + AttributeInMemory.of(cmd.attribute))
            }

            is ModelRepoCmd.UpdateEntityAttribute ->
                modifyingEntityAttribute(model, cmd.entityId, cmd.attributeId) { a ->
                    updateEntityAttribute(model, a, cmd)
                }

            is ModelRepoCmd.UpdateEntityAttributeTagAdd ->
                modifyingEntityAttribute(model, cmd.entityId, cmd.attributeId) { a ->
                    a.copy(tags = tagAdd(a.tags, cmd.tagId))
                }

            is ModelRepoCmd.UpdateEntityAttributeTagDelete ->
                modifyingEntityAttribute(model, cmd.entityId, cmd.attributeId) { a ->
                    a.copy(tags = tagDelete(a.tags, cmd.tagId))
                }

            is ModelRepoCmd.DeleteEntityAttribute ->
                modifyingEntityAttribute(model, cmd.entityId, cmd.attributeId) { null }

            is ModelRepoCmd.CreateRelationship -> model.copy(
                relationships = model.relationships + RelationshipInMemory.of(cmd.initializer)
            )

            is ModelRepoCmd.UpdateRelationship ->
                modifyingRelationship(model, cmd.relationshipId) { rel ->
                    updateRelationship(model, rel, cmd)
                }

            is ModelRepoCmd.UpdateRelationshipTagAdd ->
                modifyingRelationship(model, cmd.relationshipId) { rel ->
                    rel.copy(tags = tagAdd(rel.tags, cmd.tagId))
                }

            is ModelRepoCmd.UpdateRelationshipTagDelete ->
                modifyingRelationship(model, cmd.relationshipId) { rel ->
                    rel.copy(tags = tagDelete(rel.tags, cmd.tagId))
                }


            is ModelRepoCmd.DeleteRelationship -> model.copy(
                relationships = model.relationships.filter { it.id != cmd.relationshipId  }
            )

            is ModelRepoCmd.DeleteRelationshipAttribute -> model.copy(
                relationships = model.relationships.map { rel ->
                    if (rel.id != cmd.relationshipId) rel else rel.copy(
                        attributes = rel.attributes.filter { attr -> attr.id != cmd.attributeId })
                }
            )

            is ModelRepoCmd.UpdateRelationshipAttribute ->
                modifyingRelationshipAttribute(model, cmd.relationshipId, cmd.attributeId) { attr ->
                    updateRelationshipAttribute(model, attr, cmd)
                }


            is ModelRepoCmd.UpdateRelationshipAttributeTagAdd ->
                modifyingRelationshipAttribute(model, cmd.relationshipId, cmd.attributeId) { attr ->
                    attr.copy(tags = tagAdd(attr.tags, cmd.tagId))
                }

            is ModelRepoCmd.UpdateRelationshipAttributeTagDelete ->
                modifyingRelationshipAttribute(model, cmd.relationshipId, cmd.attributeId) { attr ->
                    attr.copy(tags = tagDelete(attr.tags, cmd.tagId))
                }

            is ModelRepoCmd.CreateRelationshipAttribute -> model.copy(
                relationships = model.relationships.map { rel ->
                    if (rel.id != cmd.relationshipId) rel else rel.copy(
                        attributes = rel.attributes + AttributeInMemory.of(cmd.attr)
                    )
                }
            )

        }
    }


    private fun updateEntityAttribute(
        model: ModelInMemory,
        attribute: AttributeInMemory,
        cmd: ModelRepoCmd.UpdateEntityAttribute
    ): AttributeInMemory? = when (val input = cmd.cmd) {
        is ModelRepoCmdAttributeUpdate.Key -> attribute.copy(key = input.value)
        is ModelRepoCmdAttributeUpdate.Name -> attribute.copy(name = input.value)
        is ModelRepoCmdAttributeUpdate.Description -> attribute.copy(description = input.value)
        is ModelRepoCmdAttributeUpdate.Type -> {
            val type = findType(model, input.value)
            attribute.copy(typeId = type.id)
        }
        is ModelRepoCmdAttributeUpdate.Optional -> attribute.copy(optional = input.value)
    }

    private fun updateRelationshipAttribute(
        model: ModelInMemory,
        attribute: AttributeInMemory,
        cmd: ModelRepoCmd.UpdateRelationshipAttribute,
    ): AttributeInMemory = when (val input = cmd.cmd) {
        is ModelRepoCmdAttributeUpdate.Key -> attribute.copy(key = input.value)
        is ModelRepoCmdAttributeUpdate.Name -> attribute.copy(name = input.value)
        is ModelRepoCmdAttributeUpdate.Description -> attribute.copy(description = input.value)
        is ModelRepoCmdAttributeUpdate.Optional -> attribute.copy(optional = input.value)
        is ModelRepoCmdAttributeUpdate.Type -> {
            val type = findType(model, input.value)
            attribute.copy(typeId = type.id)
        }
    }
    private fun findType(model: ModelInMemory, typeId:TypeId): ModelType {
        return model.findTypeOptional(typeId) ?: throw TypeNotFoundException(ModelRef.ById(model.id), TypeRef.ById(typeId))
    }
}



private fun updateRelationship(
    model: ModelInMemory,
    rel: RelationshipInMemory,
    cmd: ModelRepoCmd.UpdateRelationship,
): RelationshipInMemory = when (val input = cmd.cmd) {
    is ModelRepoCmdRelationshipUpdate.Key -> rel.copy(key = input.value)
    is ModelRepoCmdRelationshipUpdate.Name -> rel.copy(name = input.value)
    is ModelRepoCmdRelationshipUpdate.Description -> rel.copy(description = input.value)
    is ModelRepoCmdRelationshipUpdate.RoleKey -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            key = cmd.cmd.value
        )
    })

    is ModelRepoCmdRelationshipUpdate.RoleEntity -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else {
            val entity = model.findEntityOptional(cmd.cmd.value) ?: throw EntityNotFoundException(ModelRef.ById(model.id), EntityRef.ById(cmd.cmd.value))
            role.copy(
                entityId = entity.id
            )
        }
    })

    is ModelRepoCmdRelationshipUpdate.RoleName -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            name = cmd.cmd.value
        )
    })

    is ModelRepoCmdRelationshipUpdate.RoleCardinality -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            cardinality = cmd.cmd.value
        )
    })
}


private fun modifyingEntityAttribute(
    model: ModelInMemory,
    entityId: EntityId,
    attributeId: AttributeId,
    block: (AttributeInMemory) -> AttributeInMemory?
): ModelInMemory {
    return model.copy(
        entities = model.entities.map { entity ->
            if (entity.id != entityId) entity else entity.copy(
                attributes = entity.attributes.mapNotNull { attr ->
                    if (attr.id != attributeId) attr else block(attr)
                }
            )
        })
}

private fun modifyingRelationshipAttribute(
    model: ModelInMemory,
    relationshipId: RelationshipId,
    attributeId: AttributeId,
    block: (AttributeInMemory) -> AttributeInMemory?
): ModelInMemory {
    return model.copy(
        relationships = model.relationships.map { relDef ->
            if (relDef.id != relationshipId) relDef else relDef.copy(
                attributes = relDef.attributes.mapNotNull { attr ->
                    if (attr.id != attributeId) attr else block(attr)
                }
            )
        })
}


private fun modifyingEntity(
    model: ModelInMemory,
    entityId: EntityId,
    block: (EntityInMemory) -> EntityInMemory?
): ModelInMemory {
    return model.copy(
        entities = model.entities.mapNotNull { entity ->
            if (entity.id != entityId) entity else block(entity)
        })
}

private fun modifyingRelationship(
    model: ModelInMemory,
    relationshipId: RelationshipId,
    block: (RelationshipInMemory) -> RelationshipInMemory?
): ModelInMemory {
    return model.copy(
        relationships = model.relationships.mapNotNull { rel ->
            if (rel.id != relationshipId) rel else block(rel)
        })
}

private fun tagAdd(tags: List<TagId>, next: TagId): List<TagId> {
    return if (!tags.contains(next)) tags.plus(next) else tags
}

private fun tagDelete(tags: List<TagId>, next: TagId): List<TagId> {
    return tags.filter { it != next }
}

class ModelInMemoryReducerCommandNotSupportedException(cmd: ModelRepoCmd) :
    MedatarunException("Command not supported in Memory reducer : $cmd")
