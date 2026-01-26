package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelTypeUpdateCmd
import io.medatarun.model.ports.needs.*

class ModelInMemoryReducer {
    fun dispatch(model: ModelInMemory, cmd: ModelRepoCmdOnModel): ModelInMemory {
        return when (cmd) {


            is ModelRepoCmd.UpdateModelDescription -> model.copy(description = cmd.description)
            is ModelRepoCmd.UpdateModelName -> model.copy(name = cmd.name)
            is ModelRepoCmd.UpdateModelVersion -> model.copy(version = cmd.version)
            is ModelRepoCmd.UpdateModelDocumentationHome -> model.copy(documentationHome = cmd.url)
            is ModelRepoCmd.UpdateModelHashtagAdd -> model.copy(
                hashtags = hashtagAdd(
                    model.hashtags,
                    cmd.hashtag
                )
            )

            is ModelRepoCmd.UpdateModelHashtagDelete -> model.copy(
                hashtags = hashtagDelete(
                    model.hashtags,
                    cmd.hashtag
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
                }
            })

            is ModelRepoCmd.DeleteType -> model.copy(types = model.types.mapNotNull { type -> if (type.id != cmd.typeId) type else null })

            is ModelRepoCmd.CreateEntity -> model.copy(entityDefs = model.entityDefs + EntityDefInMemory.of(cmd.entityDef))
            is ModelRepoCmd.UpdateEntity -> modifyingEntityDef(model, cmd.entityId) { previous ->
                when (val c = cmd.cmd) {
                    is ModelRepoCmdEntityUpdate.Key -> previous.copy(key = c.value)
                    is ModelRepoCmdEntityUpdate.Name -> previous.copy(name = c.value)
                    is ModelRepoCmdEntityUpdate.Description -> previous.copy(description = c.value)
                    is ModelRepoCmdEntityUpdate.IdentifierAttribute -> {
                        val entityRef = EntityRef.ById(cmd.entityId)
                        val attr = model.findEntityAttributeOptional(entityRef, cmd.cmd.value)
                            ?: throw EntityAttributeNotFoundException(ModelRef.ById(model.id), entityRef, EntityAttributeRef.ById(cmd.cmd.value))
                        previous.copy(identifierAttributeKey = attr.key)
                    }
                    is ModelRepoCmdEntityUpdate.DocumentationHome -> previous.copy(documentationHome = c.value)
                }
            }

            is ModelRepoCmd.UpdateEntityHashtagAdd ->
                modifyingEntityDef(model, cmd.entityId) { previous ->
                    previous.copy(hashtags = hashtagAdd(previous.hashtags, cmd.hashtag))
                }

            is ModelRepoCmd.UpdateEntityHashtagDelete ->
                modifyingEntityDef(model, cmd.entityId) { previous ->
                    previous.copy(hashtags = hashtagDelete(previous.hashtags, cmd.hashtag))
                }

            is ModelRepoCmd.DeleteEntity -> modifyingEntityDef(model, cmd.entityId) { null }

            is ModelRepoCmd.CreateEntityAttribute -> modifyingEntityDef(model, cmd.entityId) {
                it.copy(attributes = it.attributes + AttributeDefInMemory.of(cmd.attributeDef))
            }

            is ModelRepoCmd.UpdateEntityAttribute ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeId) { a ->
                    updateEntityAttribute(model, a, cmd)
                }

            is ModelRepoCmd.UpdateEntityAttributeHashtagAdd ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeId) { a ->
                    a.copy(hashtags = hashtagAdd(a.hashtags, cmd.hashtag))
                }

            is ModelRepoCmd.UpdateEntityAttributeHashtagDelete ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeId) { a ->
                    a.copy(hashtags = hashtagDelete(a.hashtags, cmd.hashtag))
                }

            is ModelRepoCmd.DeleteEntityAttribute ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeId) { null }

            is ModelRepoCmd.CreateRelationship -> model.copy(
                relationshipDefs = model.relationshipDefs + RelationshipDefInMemory.of(cmd.initializer)
            )

            is ModelRepoCmd.UpdateRelationship ->
                modifyingRelationshipDef(model, cmd.relationshipId) { rel ->
                    updateRelationship(model, rel, cmd)
                }

            is ModelRepoCmd.UpdateRelationshipHashtagAdd ->
                modifyingRelationshipDef(model, cmd.relationshipId) { rel ->
                    rel.copy(hashtags = hashtagAdd(rel.hashtags, cmd.hashtag))
                }

            is ModelRepoCmd.UpdateRelationshipHashtagDelete ->
                modifyingRelationshipDef(model, cmd.relationshipId) { rel ->
                    rel.copy(hashtags = hashtagDelete(rel.hashtags, cmd.hashtag))
                }


            is ModelRepoCmd.DeleteRelationship -> model.copy(
                relationshipDefs = model.relationshipDefs.filter { it.id != cmd.relationshipId  }
            )

            is ModelRepoCmd.DeleteRelationshipAttribute -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipId) rel else rel.copy(
                        attributes = rel.attributes.filter { attr -> attr.id != cmd.attributeId })
                }
            )

            is ModelRepoCmd.UpdateRelationshipAttribute ->
                modifyingRelationshipDefAttributeDef(model, cmd.relationshipId, cmd.attributeId) { attr ->
                    updateRelationshipAttribute(model, attr, cmd)
                }


            is ModelRepoCmd.UpdateRelationshipAttributeHashtagAdd ->
                modifyingRelationshipDefAttributeDef(model, cmd.relationshipId, cmd.attributeId) { attr ->
                    attr.copy(hashtags = hashtagAdd(attr.hashtags, cmd.hashtag))
                }

            is ModelRepoCmd.UpdateRelationshipAttributeHashtagDelete ->
                modifyingRelationshipDefAttributeDef(model, cmd.relationshipId, cmd.attributeId) { attr ->
                    attr.copy(hashtags = hashtagDelete(attr.hashtags, cmd.hashtag))
                }

            is ModelRepoCmd.CreateRelationshipAttribute -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipId) rel else rel.copy(
                        attributes = rel.attributes + AttributeDefInMemory.of(cmd.attr)
                    )
                }
            )

        }
    }


    private fun updateEntityAttribute(
        model: ModelInMemory,
        attribute: AttributeDefInMemory,
        cmd: ModelRepoCmd.UpdateEntityAttribute
    ): AttributeDefInMemory? = when (val input = cmd.cmd) {
        is ModelRepoCmdAttributeUpdate.Key -> attribute.copy(key = input.value)
        is ModelRepoCmdAttributeUpdate.Name -> attribute.copy(name = input.value)
        is ModelRepoCmdAttributeUpdate.Description -> attribute.copy(description = input.value)
        is ModelRepoCmdAttributeUpdate.Type -> {
            val type = findType(model, input.value)
            attribute.copy(type = type.key)
        }
        is ModelRepoCmdAttributeUpdate.Optional -> attribute.copy(optional = input.value)
    }

    private fun updateRelationshipAttribute(
        model: ModelInMemory,
        attribute: AttributeDefInMemory,
        cmd: ModelRepoCmd.UpdateRelationshipAttribute,
    ): AttributeDefInMemory = when (val input = cmd.cmd) {
        is ModelRepoCmdAttributeUpdate.Key -> attribute.copy(key = input.value)
        is ModelRepoCmdAttributeUpdate.Name -> attribute.copy(name = input.value)
        is ModelRepoCmdAttributeUpdate.Description -> attribute.copy(description = input.value)
        is ModelRepoCmdAttributeUpdate.Optional -> attribute.copy(optional = input.value)
        is ModelRepoCmdAttributeUpdate.Type -> {
            val type = findType(model, input.value)
            attribute.copy(type = type.key)
        }
    }
    private fun findType(model: ModelInMemory, typeId:TypeId): ModelType {
        return model.findTypeOptional(typeId) ?: throw TypeNotFoundException(ModelRef.ById(model.id), TypeRef.ById(typeId))
    }
}



private fun updateRelationship(
    model: ModelInMemory,
    rel: RelationshipDefInMemory,
    cmd: ModelRepoCmd.UpdateRelationship,
): RelationshipDefInMemory = when (val input = cmd.cmd) {
    is ModelRepoCmdRelationshipDefUpdate.Key -> rel.copy(key = input.value)
    is ModelRepoCmdRelationshipDefUpdate.Name -> rel.copy(name = input.value)
    is ModelRepoCmdRelationshipDefUpdate.Description -> rel.copy(description = input.value)
    is ModelRepoCmdRelationshipDefUpdate.RoleKey -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            key = cmd.cmd.value
        )
    })

    is ModelRepoCmdRelationshipDefUpdate.RoleEntity -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else {
            val entity = model.findEntityOptional(cmd.cmd.value) ?: throw EntityNotFoundException(ModelRef.ById(model.id), EntityRef.ById(cmd.cmd.value))
            role.copy(
                entityKey = entity.key
            )
        }
    })

    is ModelRepoCmdRelationshipDefUpdate.RoleName -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            name = cmd.cmd.value
        )
    })

    is ModelRepoCmdRelationshipDefUpdate.RoleCardinality -> rel.copy(roles = rel.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            cardinality = cmd.cmd.value
        )
    })
}


private fun modifyingEntityDefAttributeDef(
    model: ModelInMemory,
    entityId: EntityId,
    attributeId: AttributeId,
    block: (AttributeDefInMemory) -> AttributeDefInMemory?
): ModelInMemory {
    return model.copy(
        entityDefs = model.entityDefs.map { entityDef ->
            if (entityDef.id != entityId) entityDef else entityDef.copy(
                attributes = entityDef.attributes.mapNotNull { attr ->
                    if (attr.id != attributeId) attr else block(attr)
                }
            )
        })
}

private fun modifyingRelationshipDefAttributeDef(
    model: ModelInMemory,
    relationshipId: RelationshipId,
    attributeId: AttributeId,
    block: (AttributeDefInMemory) -> AttributeDefInMemory?
): ModelInMemory {
    return model.copy(
        relationshipDefs = model.relationshipDefs.map { relDef ->
            if (relDef.id != relationshipId) relDef else relDef.copy(
                attributes = relDef.attributes.mapNotNull { attr ->
                    if (attr.id != attributeId) attr else block(attr)
                }
            )
        })
}


private fun modifyingEntityDef(
    model: ModelInMemory,
    entityId: EntityId,
    block: (EntityDefInMemory) -> EntityDefInMemory?
): ModelInMemory {
    return model.copy(
        entityDefs = model.entityDefs.mapNotNull { entityDef ->
            if (entityDef.id != entityId) entityDef else block(entityDef)
        })
}

private fun modifyingRelationshipDef(
    model: ModelInMemory,
    relationshipId: RelationshipId,
    block: (RelationshipDefInMemory) -> RelationshipDefInMemory?
): ModelInMemory {
    return model.copy(
        relationshipDefs = model.relationshipDefs.mapNotNull { rel ->
            if (rel.id != relationshipId) rel else block(rel)
        })
}

private fun hashtagAdd(hashtags: List<Hashtag>, next: Hashtag): List<Hashtag> {
    return if (!hashtags.contains(next)) hashtags.plus(next) else hashtags
}

private fun hashtagDelete(hashtags: List<Hashtag>, next: Hashtag): List<Hashtag> {
    return hashtags.filter { it != next }
}

class ModelInMemoryReducerCommandNotSupportedException(cmd: ModelRepoCmd) :
    MedatarunException("Command not supported in Memory reducer : $cmd")