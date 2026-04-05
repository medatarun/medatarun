package io.medatarun.auth.domain

import io.medatarun.type.commons.key.Key

@JvmInline
value class PermissionKey(override val value: String) : Key<PermissionKey>
