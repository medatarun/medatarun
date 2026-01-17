package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*

sealed interface RelationshipDefUpdateCmd {
    class Key(val value: RelationshipKey) : RelationshipDefUpdateCmd
    class Name(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class Description(val value: LocalizedMarkdown?) : RelationshipDefUpdateCmd
    class RoleKey(val relationshipRoleKey: RelationshipRoleKey, val value: RelationshipRoleKey) : RelationshipDefUpdateCmd
    class RoleName(val relationshipRoleKey: RelationshipRoleKey, val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleEntity(val relationshipRoleKey: RelationshipRoleKey, val value: EntityKey) : RelationshipDefUpdateCmd
    class RoleCardinality(val relationshipRoleKey: RelationshipRoleKey, val value: RelationshipCardinality) : RelationshipDefUpdateCmd
}