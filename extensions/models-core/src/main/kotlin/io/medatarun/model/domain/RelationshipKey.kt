package io.medatarun.model.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class RelationshipKey(override val value: String): Key<RelationshipKey>