package io.medatarun.auth.domain.role

import io.medatarun.type.commons.key.Key

@JvmInline
value class RoleKey(override val value: String) : Key<RoleKey>