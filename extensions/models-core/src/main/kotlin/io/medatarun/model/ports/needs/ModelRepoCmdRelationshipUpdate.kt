package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*

sealed interface ModelRepoCmdRelationshipUpdate {
    class Key(val value: RelationshipKey) : ModelRepoCmdRelationshipUpdate
    class Name(val value: LocalizedText?) : ModelRepoCmdRelationshipUpdate
    class Description(val value: LocalizedMarkdown?) : ModelRepoCmdRelationshipUpdate
    class RoleKey(val relationshipRoleId: RelationshipRoleId, val value: RelationshipRoleKey) : ModelRepoCmdRelationshipUpdate
    class RoleName(val relationshipRoleId: RelationshipRoleId, val value: LocalizedText?) : ModelRepoCmdRelationshipUpdate
    class RoleEntity(val relationshipRoleId: RelationshipRoleId, val value: EntityId) : ModelRepoCmdRelationshipUpdate
    class RoleCardinality(val relationshipRoleId: RelationshipRoleId, val value: RelationshipCardinality) : ModelRepoCmdRelationshipUpdate
}