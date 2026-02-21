package io.medatarun.model.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class TypeKey(override val value: String) : Key<TypeKey>