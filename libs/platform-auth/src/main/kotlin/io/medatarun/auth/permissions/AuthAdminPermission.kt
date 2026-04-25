package io.medatarun.auth.permissions

import io.medatarun.auth.domain.ActorPermission.Companion.ADMIN
import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey


object AuthAdminPermission: AppPermission {
    override val key = AppPermissionKey(ADMIN.key)
    override val name: String = "Administrator"
    override val description: String = "Grants user or tool administrator privileges."
    override val category: AppPermissionCategory = AppPermissionCategory.ADMIN_SCOPE
}