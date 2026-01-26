package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.AttributeDefUpdateCmd
import io.medatarun.model.ports.exposed.EntityDefUpdateCmd
import io.medatarun.model.ports.exposed.ModelTypeUpdateCmd
import io.medatarun.model.ports.exposed.RelationshipDefUpdateCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmdOnModel

class ModelInMemoryReducer {
    fun dispatch(model: ModelInMemory, cmd: ModelRepositoryCmdOnModel): ModelInMemory {
        return when (cmd) {


            is ModelRepositoryCmd.UpdateModelDescription -> model.copy(description = cmd.description)
            is ModelRepositoryCmd.UpdateModelName -> model.copy(name = cmd.name)
            is ModelRepositoryCmd.UpdateModelVersion -> model.copy(version = cmd.version)
            is ModelRepositoryCmd.UpdateModelDocumentationHome -> model.copy(documentationHome = cmd.url)
            is ModelRepositoryCmd.UpdateModelHashtagAdd -> model.copy(
                hashtags = hashtagAdd(
                    model.hashtags,
                    cmd.hashtag
                )
            )

            is ModelRepositoryCmd.UpdateModelHashtagDelete -> model.copy(
                hashtags = hashtagDelete(
                    model.hashtags,
                    cmd.hashtag
                )
            )

            is ModelRepositoryCmd.DeleteModel -> throw ModelInMemoryReducerCommandNotSupportedException(cmd)

            is ModelRepositoryCmd.CreateType -> model.copy(
                types = model.types + ModelTypeInMemory(
                    id = TypeId.generate(),
                    key = cmd.initializer.id,
                    name = cmd.initializer.name,
                    description = cmd.initializer.description
                )
            )

            is ModelRepositoryCmd.UpdateType -> model.copy(types = model.types.map { type ->
                if (type.id != cmd.typeId) type else when (cmd.cmd) {
                    is ModelTypeUpdateCmd.Name -> type.copy(name = cmd.cmd.value)
                    is ModelTypeUpdateCmd.Description -> type.copy(description = cmd.cmd.value)
                }
            })

            is ModelRepositoryCmd.DeleteType -> model.copy(types = model.types.mapNotNull { type -> if (type.id != cmd.typeId) type else null })

            is ModelRepositoryCmd.CreateEntityDef -> model.copy(entityDefs = model.entityDefs + EntityDefInMemory.of(cmd.entityDef))
            is ModelRepositoryCmd.UpdateEntityDef -> modifyingEntityDef(model, cmd.entityId) { previous ->
                when (val c = cmd.cmd) {
                    is EntityDefUpdateCmd.Key -> previous.copy(key = c.value)
                    is EntityDefUpdateCmd.Name -> previous.copy(name = c.value)
                    is EntityDefUpdateCmd.Description -> previous.copy(description = c.value)
                    is EntityDefUpdateCmd.IdentifierAttribute -> previous.copy(identifierAttributeKey = c.value)
                    is EntityDefUpdateCmd.DocumentationHome -> previous.copy(documentationHome = c.value)
                }
            }

            is ModelRepositoryCmd.UpdateEntityDefHashtagAdd ->
                modifyingEntityDef(model, cmd.entityId) { previous ->
                    previous.copy(hashtags = hashtagAdd(previous.hashtags, cmd.hashtag))
                }

            is ModelRepositoryCmd.UpdateEntityDefHashtagDelete ->
                modifyingEntityDef(model, cmd.entityId) { previous ->
                    previous.copy(hashtags = hashtagDelete(previous.hashtags, cmd.hashtag))
                }

            is ModelRepositoryCmd.DeleteEntityDef -> modifyingEntityDef(model, cmd.entityId) { null }

            is ModelRepositoryCmd.CreateEntityDefAttributeDef -> modifyingEntityDef(model, cmd.entityId) {
                it.copy(attributes = it.attributes + AttributeDefInMemory.of(cmd.attributeDef))
            }

            is ModelRepositoryCmd.UpdateEntityDefAttributeDef ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeKey) { a ->
                    updateEntityAttribute(a, cmd)
                }

            is ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagAdd ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeKey) { a ->
                    a.copy(hashtags = hashtagAdd(a.hashtags, cmd.hashtag))
                }

            is ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagDelete ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeKey) { a ->
                    a.copy(hashtags = hashtagDelete(a.hashtags, cmd.hashtag))
                }

            is ModelRepositoryCmd.DeleteEntityDefAttributeDef ->
                modifyingEntityDefAttributeDef(model, cmd.entityId, cmd.attributeKey) { null }

            is ModelRepositoryCmd.CreateRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs + RelationshipDefInMemory.of(cmd.initializer)
            )

            is ModelRepositoryCmd.UpdateRelationshipDef ->
                modifyingRelationshipDef(model, cmd.relationshipKey) { rel ->
                    updateRelationship(rel, cmd)
                }

            is ModelRepositoryCmd.UpdateRelationshipDefHashtagAdd ->
                modifyingRelationshipDef(model, cmd.relationshipKey) { rel ->
                    rel.copy(hashtags = hashtagAdd(rel.hashtags, cmd.hashtag))
                }

            is ModelRepositoryCmd.UpdateRelationshipDefHashtagDelete ->
                modifyingRelationshipDef(model, cmd.relationshipKey) { rel ->
                    rel.copy(hashtags = hashtagDelete(rel.hashtags, cmd.hashtag))
                }


            is ModelRepositoryCmd.DeleteRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs.filter { it.key != cmd.relationshipKey }
            )

            is ModelRepositoryCmd.DeleteRelationshipAttributeDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.key != cmd.relationshipKey) rel else rel.copy(
                        attributes = rel.attributes.filter { attr -> attr.key != cmd.attributeKey })
                }
            )

            is ModelRepositoryCmd.UpdateRelationshipAttributeDef ->
                modifyingRelationshipDefAttributeDef(model, cmd.relationshipKey, cmd.attributeKey) { attr ->
                    updateRelationshipAttribute(attr, cmd)
                }


            is ModelRepositoryCmd.UpdateRelationshipAttributeDefHashtagAdd ->
                modifyingRelationshipDefAttributeDef(model, cmd.relationshipKey, cmd.attributeKey) { attr ->
                    attr.copy(hashtags = hashtagAdd(attr.hashtags, cmd.hashtag))
                }

            is ModelRepositoryCmd.UpdateRelationshipAttributeDefHashtagDelete ->
                modifyingRelationshipDefAttributeDef(model, cmd.relationshipKey, cmd.attributeKey) { attr ->
                    attr.copy(hashtags = hashtagDelete(attr.hashtags, cmd.hashtag))
                }

            is ModelRepositoryCmd.CreateRelationshipAttributeDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.key != cmd.relationshipKey) rel else rel.copy(
                        attributes = rel.attributes + AttributeDefInMemory.of(cmd.attr)
                    )
                }
            )

        }
    }


}

private fun updateEntityAttribute(
    attribute: AttributeDefInMemory,
    cmd: ModelRepositoryCmd.UpdateEntityDefAttributeDef
): AttributeDefInMemory? = when (val input = cmd.cmd) {
    is AttributeDefUpdateCmd.Key -> attribute.copy(key = input.value)
    is AttributeDefUpdateCmd.Name -> attribute.copy(name = input.value)
    is AttributeDefUpdateCmd.Description -> attribute.copy(description = input.value)
    is AttributeDefUpdateCmd.Type -> attribute.copy(type = input.value)
    is AttributeDefUpdateCmd.Optional -> attribute.copy(optional = input.value)
}

private fun updateRelationshipAttribute(
    attribute: AttributeDefInMemory,
    cmd: ModelRepositoryCmd.UpdateRelationshipAttributeDef,
): AttributeDefInMemory = when (val input = cmd.cmd) {
    is AttributeDefUpdateCmd.Key -> attribute.copy(key = input.value)
    is AttributeDefUpdateCmd.Name -> attribute.copy(name = input.value)
    is AttributeDefUpdateCmd.Description -> attribute.copy(description = input.value)
    is AttributeDefUpdateCmd.Optional -> attribute.copy(optional = input.value)
    is AttributeDefUpdateCmd.Type -> attribute.copy(type = input.value)
}

private fun updateRelationship(
    rel: RelationshipDefInMemory,
    cmd: ModelRepositoryCmd.UpdateRelationshipDef,
): RelationshipDefInMemory = when (val input = cmd.cmd) {
    is RelationshipDefUpdateCmd.Key -> rel.copy(key = input.value)
    is RelationshipDefUpdateCmd.Name -> rel.copy(name = input.value)
    is RelationshipDefUpdateCmd.Description -> rel.copy(description = input.value)
    is RelationshipDefUpdateCmd.RoleKey -> rel.copy(roles = rel.roles.map { role ->
        if (role.key != cmd.cmd.relationshipRoleKey) role else role.copy(
            key = cmd.cmd.value
        )
    })

    is RelationshipDefUpdateCmd.RoleEntity -> rel.copy(roles = rel.roles.map { role ->
        if (role.key != cmd.cmd.relationshipRoleKey) role else role.copy(
            entityId = cmd.cmd.value
        )
    })

    is RelationshipDefUpdateCmd.RoleName -> rel.copy(roles = rel.roles.map { role ->
        if (role.key != cmd.cmd.relationshipRoleKey) role else role.copy(
            name = cmd.cmd.value
        )
    })

    is RelationshipDefUpdateCmd.RoleCardinality -> rel.copy(roles = rel.roles.map { role ->
        if (role.key != cmd.cmd.relationshipRoleKey) role else role.copy(
            cardinality = cmd.cmd.value
        )
    })
}


private fun modifyingEntityDefAttributeDef(
    model: ModelInMemory,
    entityId: EntityId,
    attributeKey: AttributeKey,
    block: (AttributeDefInMemory) -> AttributeDefInMemory?
): ModelInMemory {
    return model.copy(
        entityDefs = model.entityDefs.map { entityDef ->
            if (entityDef.id != entityId) entityDef else entityDef.copy(
                attributes = entityDef.attributes.mapNotNull { attr ->
                    if (attr.key != attributeKey) attr else block(attr)
                }
            )
        })
}

private fun modifyingRelationshipDefAttributeDef(
    model: ModelInMemory,
    r: RelationshipKey,
    attributeKey: AttributeKey,
    block: (AttributeDefInMemory) -> AttributeDefInMemory?
): ModelInMemory {
    return model.copy(
        relationshipDefs = model.relationshipDefs.map { relDef ->
            if (relDef.key != r) relDef else relDef.copy(
                attributes = relDef.attributes.mapNotNull { attr ->
                    if (attr.key != attributeKey) attr else block(attr)
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
    e: RelationshipKey,
    block: (RelationshipDefInMemory) -> RelationshipDefInMemory?
): ModelInMemory {
    return model.copy(
        relationshipDefs = model.relationshipDefs.mapNotNull { rel ->
            if (rel.key != e) rel else block(rel)
        })
}

private fun hashtagAdd(hashtags: List<Hashtag>, next: Hashtag): List<Hashtag> {
    return if (!hashtags.contains(next)) hashtags.plus(next) else hashtags
}

private fun hashtagDelete(hashtags: List<Hashtag>, next: Hashtag): List<Hashtag> {
    return hashtags.filter { it != next }
}

class ModelInMemoryReducerCommandNotSupportedException(cmd: ModelRepositoryCmd) :
    MedatarunException("Command not supported in Memory reducer : $cmd")