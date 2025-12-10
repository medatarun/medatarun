package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipRoleId

sealed interface RelationshipDefUpdateCmd {
    class Name(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class Description(val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleName(val relationshipRoleId: RelationshipRoleId, val value: LocalizedText?) : RelationshipDefUpdateCmd
    class RoleCardinality(val relationshipRoleId: RelationshipRoleId, val value: RelationshipCardinality) : RelationshipDefUpdateCmd
}