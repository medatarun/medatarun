package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*

sealed interface RelationshipUpdateCmd {
    class Key(val value: RelationshipKey) : RelationshipUpdateCmd
    class Name(val value: LocalizedText?) : RelationshipUpdateCmd
    class Description(val value: LocalizedMarkdown?) : RelationshipUpdateCmd
    class RoleKey(val relationshipRoleRef: RelationshipRoleRef, val value: RelationshipRoleKey) : RelationshipUpdateCmd
    class RoleName(val relationshipRoleRef: RelationshipRoleRef, val value: LocalizedText?) : RelationshipUpdateCmd
    class RoleEntity(val relationshipRoleRef: RelationshipRoleRef, val value: EntityRef) : RelationshipUpdateCmd
    class RoleCardinality(val relationshipRoleRef: RelationshipRoleRef, val value: RelationshipCardinality) : RelationshipUpdateCmd
}