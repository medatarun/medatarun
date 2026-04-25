package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object ModelUpdateAuthorityPermission : AppPermission {
    override val key = AppPermissionKey("model_update_authority")
    override val name: String = "Models: change autority"
    override val description: String = "Allow actor to change model authority (from system to canonical)."
    override val implies = listOf(ModelReadPermission.key)
    override val category: AppPermissionCategory = AppPermissionCategory.WRITE
}