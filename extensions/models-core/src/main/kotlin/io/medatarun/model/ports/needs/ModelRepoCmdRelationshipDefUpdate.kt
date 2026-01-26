package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*

sealed interface ModelRepoCmdRelationshipDefUpdate {
    class Key(val value: RelationshipKey) : ModelRepoCmdRelationshipDefUpdate
    class Name(val value: LocalizedText?) : ModelRepoCmdRelationshipDefUpdate
    class Description(val value: LocalizedMarkdown?) : ModelRepoCmdRelationshipDefUpdate
    class RoleKey(val relationshipRoleId: RelationshipRoleId, val value: RelationshipRoleKey) : ModelRepoCmdRelationshipDefUpdate
    class RoleName(val relationshipRoleId: RelationshipRoleId, val value: LocalizedText?) : ModelRepoCmdRelationshipDefUpdate
    class RoleEntity(val relationshipRoleId: RelationshipRoleId, val value: EntityId) : ModelRepoCmdRelationshipDefUpdate
    class RoleCardinality(val relationshipRoleId: RelationshipRoleId, val value: RelationshipCardinality) : ModelRepoCmdRelationshipDefUpdate
}