package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityRef
import io.medatarun.type.commons.text.TextSingleLine
import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipRoleKey

data class RelationshipInitializerRole(
    val key: RelationshipRoleKey,
    val name: TextSingleLine?,
    val entityRef: EntityRef,
    val cardinality: RelationshipCardinality
)
