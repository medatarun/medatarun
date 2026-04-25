package io.medatarun.security

import io.medatarun.type.commons.key.Key

@JvmInline
value class AppPermissionKey(override val value: String) : Key<AppPermissionKey>