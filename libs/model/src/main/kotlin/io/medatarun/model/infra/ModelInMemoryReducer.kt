package io.medatarun.model.infra

import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.MedatarunException
import io.medatarun.model.ports.exposed.AttributeDefUpdateCmd
import io.medatarun.model.ports.exposed.EntityDefUpdateCmd
import io.medatarun.model.ports.exposed.ModelTypeUpdateCmd
import io.medatarun.model.ports.exposed.RelationshipDefUpdateCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmdOnModel

class ModelInMemoryReducer() {
    fun dispatch(model: ModelInMemory, cmd: ModelRepositoryCmdOnModel): ModelInMemory {
        return when (cmd) {


            is ModelRepositoryCmd.UpdateModelDescription -> model.copy(description = cmd.description)
            is ModelRepositoryCmd.UpdateModelName -> model.copy(name = cmd.name)
            is ModelRepositoryCmd.UpdateModelVersion -> model.copy(version = cmd.version)
            is ModelRepositoryCmd.DeleteModel -> throw ModelInMemoryReducerCommandNotSupportedException(cmd)

            is ModelRepositoryCmd.CreateType -> model.copy(
                types = model.types + ModelTypeInMemory(
                    id = cmd.initializer.id,
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
            is ModelRepositoryCmd.UpdateEntityDef -> modifyingEntityDef(model, cmd.entityDefId) { previous ->
                when (val c = cmd.cmd) {
                    is EntityDefUpdateCmd.Id -> previous.copy(id = c.value)
                    is EntityDefUpdateCmd.Name -> previous.copy(name = c.value)
                    is EntityDefUpdateCmd.Description -> previous.copy(description = c.value)
                    is EntityDefUpdateCmd.IdentifierAttribute -> previous.copy(identifierAttributeDefId = c.value)
                }
            }

            is ModelRepositoryCmd.DeleteEntityDef -> modifyingEntityDef(model, cmd.entityDefId) { null }

            is ModelRepositoryCmd.CreateEntityDefAttributeDef -> modifyingEntityDef(model, cmd.entityDefId) {
                it.copy(attributes = it.attributes + AttributeDefInMemory.of(cmd.attributeDef))
            }

            is ModelRepositoryCmd.UpdateEntityDefAttributeDef -> modifyingEntityDefAttributeDef(
                model,
                cmd.entityDefId,
                cmd.attributeDefId
            ) { a ->
                when (val target = cmd.cmd) {
                    is AttributeDefUpdateCmd.Id -> a.copy(id = target.value)
                    is AttributeDefUpdateCmd.Name -> a.copy(name = target.value)
                    is AttributeDefUpdateCmd.Description -> a.copy(description = target.value)
                    is AttributeDefUpdateCmd.Type -> a.copy(type = target.value)
                    is AttributeDefUpdateCmd.Optional -> a.copy(optional = target.value)
                }
            }

            is ModelRepositoryCmd.DeleteEntityDefAttributeDef -> modifyingEntityDefAttributeDef(
                model,
                cmd.entityDefId,
                cmd.attributeDefId
            ) { null }

            is ModelRepositoryCmd.CreateRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs + RelationshipDefInMemory.of(cmd.initializer)
            )

            is ModelRepositoryCmd.UpdateRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipDefId) rel else updateRelationship(rel, cmd)
                }
            )

            is ModelRepositoryCmd.DeleteRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs.filter { it.id != cmd.relationshipDefId }
            )

            is ModelRepositoryCmd.DeleteRelationshipAttributeDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipDefId) rel else rel.copy(
                        attributes = rel.attributes.filter { attr -> attr.id != cmd.attributeDefId })
                }
            )

            is ModelRepositoryCmd.UpdateRelationshipAttributeDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipDefId) rel else rel.copy(
                        attributes = rel.attributes.map { attr ->
                            if (attr.id != cmd.attributeDefId) attr else updateAttribute(attr, cmd)
                        })
                }
            )

            is ModelRepositoryCmd.CreateRelationshipAttributeDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipDefId) rel else rel.copy(
                        attributes = rel.attributes + AttributeDefInMemory.of(cmd.attr)
                    )
                }
            )

        }
    }

}

private fun updateAttribute(
    attr: AttributeDefInMemory,
    cmd: ModelRepositoryCmd.UpdateRelationshipAttributeDef,
): AttributeDefInMemory = when (cmd.cmd) {
    is AttributeDefUpdateCmd.Id -> attr.copy(id = attr.id)
    is AttributeDefUpdateCmd.Name -> attr.copy(name = attr.name)
    is AttributeDefUpdateCmd.Description -> attr.copy(description = attr.description)
    is AttributeDefUpdateCmd.Optional -> attr.copy(optional = attr.optional)
    is AttributeDefUpdateCmd.Type -> attr.copy(type = attr.type)
}

private fun updateRelationship(
    attr: RelationshipDefInMemory,
    cmd: ModelRepositoryCmd.UpdateRelationshipDef,
): RelationshipDefInMemory = when (cmd.cmd) {
    is RelationshipDefUpdateCmd.Name -> attr.copy(name = attr.name)
    is RelationshipDefUpdateCmd.Description -> attr.copy(description = attr.name)
    is RelationshipDefUpdateCmd.RoleName -> attr.copy(roles = attr.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            name = cmd.cmd.value
        )
    })

    is RelationshipDefUpdateCmd.RoleCardinality -> attr.copy(roles = attr.roles.map { role ->
        if (role.id != cmd.cmd.relationshipRoleId) role else role.copy(
            cardinality = cmd.cmd.value
        )
    })
}


private fun modifyingEntityDefAttributeDef(
    model: ModelInMemory,
    e: EntityDefId,
    attributeDefId: AttributeDefId,
    block: (AttributeDefInMemory) -> AttributeDefInMemory?
): ModelInMemory {
    return model.copy(
        entityDefs = model.entityDefs.map { entityDef ->
            if (entityDef.id != e) entityDef else entityDef.copy(
                attributes = entityDef.attributes.mapNotNull { attr ->
                    if (attr.id != attributeDefId) attr else block(attr)
                }
            )
        })
}


private fun modifyingEntityDef(
    model: ModelInMemory,
    e: EntityDefId,
    block: (EntityDefInMemory) -> EntityDefInMemory?
): ModelInMemory {
    return model.copy(
        entityDefs = model.entityDefs.mapNotNull { entityDef ->
            if (entityDef.id != e) entityDef else block(entityDef)
        })
}

class ModelInMemoryReducerCommandNotSupportedException(cmd: ModelRepositoryCmd) : MedatarunException("Command not supported in Memory reducer : $cmd")