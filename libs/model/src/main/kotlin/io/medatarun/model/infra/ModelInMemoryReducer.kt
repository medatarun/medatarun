package io.medatarun.model.infra

import io.medatarun.model.model.AttributeDefUpdateCmd
import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.RelationshipDefUpdateCmd

class ModelInMemoryReducer() {
    fun dispatch(model: ModelInMemory, cmd: ModelCmd): ModelInMemory {
        return when (cmd) {

            is ModelCmd.CreateRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs + RelationshipDefInMemory.of(cmd.initializer)
            )

            is ModelCmd.UpdateRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipDefId) rel else updateRelationship(rel, cmd)
                }
            )

            is ModelCmd.DeleteRelationshipDef -> model.copy(
                relationshipDefs = model.relationshipDefs.filter { it.id != cmd.relationshipDefId }
            )

            is ModelCmd.DeleteRelationshipAttributeDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipDefId) rel else rel.copy(
                        attributes = rel.attributes.filter { attr -> attr.id != cmd.attributeDefId })
                }
            )

            is ModelCmd.UpdateRelationshipAttributeDef -> model.copy(
                relationshipDefs = model.relationshipDefs.map { rel ->
                    if (rel.id != cmd.relationshipDefId) rel else rel.copy(
                        attributes = rel.attributes.map { attr ->
                            if (attr.id != cmd.attributeDefId) attr else updateAttribute(attr, cmd)
                        })
                }
            )

            is ModelCmd.CreateRelationshipAttributeDef -> model.copy(
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
    cmd: ModelCmd.UpdateRelationshipAttributeDef,
): AttributeDefInMemory = when (cmd.cmd) {
    is AttributeDefUpdateCmd.Id -> attr.copy(id = attr.id)
    is AttributeDefUpdateCmd.Name -> attr.copy(name = attr.name)
    is AttributeDefUpdateCmd.Description -> attr.copy(description = attr.description)
    is AttributeDefUpdateCmd.Optional -> attr.copy(optional = attr.optional)
    is AttributeDefUpdateCmd.Type -> attr.copy(type = attr.type)
}

private fun updateRelationship(
    attr: RelationshipDefInMemory,
    cmd: ModelCmd.UpdateRelationshipDef,
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