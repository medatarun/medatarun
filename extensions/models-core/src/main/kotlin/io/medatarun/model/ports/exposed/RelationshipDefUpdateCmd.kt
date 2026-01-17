package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*

sealed interface RelationshipDefUpdateCmd {
    class Key(val value: RelationshipKey) : RelationshipDefUpdateCmd
    class Name(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class Description(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleKey(val relationshipRoleKey: RelationshipRoleId, val value: RelationshipRoleId) : RelationshipDefUpdateCmd
    class RoleName(val relationshipRoleKey: RelationshipRoleId, val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleEntity(val relationshipRoleKey: RelationshipRoleId, val value: EntityKey) : RelationshipDefUpdateCmd
    class RoleCardinality(val relationshipRoleKey: RelationshipRoleId, val value: RelationshipCardinality) : RelationshipDefUpdateCmd
}