package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipRoleKey

data class RelationshipInitializerRole(
    val key: RelationshipRoleKey,
    val name: LocalizedText?,
    val entityRef: EntityRef,
    val cardinality: RelationshipCardinality
)
