package io.medatarun.model.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class EntityKey(override val value: String) : Key<EntityKey>