package io.medatarun.model.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class AttributeKey(override val value: String) : Key<AttributeKey>