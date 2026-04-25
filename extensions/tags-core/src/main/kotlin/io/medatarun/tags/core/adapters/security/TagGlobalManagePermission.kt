package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object TagGlobalManagePermission : AppPermission {
    override val key = AppPermissionKey("tag_global_manage")
    override val name: String = "Tags: manage global tags"
    override val description: String = "Allow user to create, delete and change global tags."
    override val category: AppPermissionCategory = AppPermissionCategory.WRITE

}