package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*

sealed interface RelationshipDefUpdateCmd {
    class Key(val value: RelationshipKey) : RelationshipDefUpdateCmd
    class Name(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class Description(val value: LocalizedMarkdown?) : RelationshipDefUpdateCmd
    class RoleKey(val relationshipRoleRef: RelationshipRoleRef, val value: RelationshipRoleKey) : RelationshipDefUpdateCmd
    class RoleName(val relationshipRoleRef: RelationshipRoleRef, val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleEntity(val relationshipRoleRef: RelationshipRoleRef, val value: EntityRef) : RelationshipDefUpdateCmd
    class RoleCardinality(val relationshipRoleRef: RelationshipRoleRef, val value: RelationshipCardinality) : RelationshipDefUpdateCmd
}