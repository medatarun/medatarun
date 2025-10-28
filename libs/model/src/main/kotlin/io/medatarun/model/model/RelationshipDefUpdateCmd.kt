package io.medatarun.model.model

sealed interface RelationshipDefUpdateCmd {
    class Name(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class Description(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleName(val relationshipRoleId: RelationshipRoleId, val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleCardinality(val relationshipRoleId: RelationshipRoleId, val value: RelationshipCardinality) : RelationshipDefUpdateCmd
}