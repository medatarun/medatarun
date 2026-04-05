package io.medatarun.auth.permissions

import io.medatarun.auth.domain.ActorPermission.Companion.ADMIN
import io.medatarun.security.AppPermission


object AuthAdminPermission: AppPermission {
    override val key: String = ADMIN.key
    override val name: String = "Administrator"
    override val description: String = "Grants user or tool administrator privileges."
}