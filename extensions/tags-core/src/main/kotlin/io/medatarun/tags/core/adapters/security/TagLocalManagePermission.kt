package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object TagLocalManagePermission : AppPermission {
    override val key = AppPermissionKey("tag_local_manage")
    override val name: String = "Tags: manage local tags"
    override val description: String = "Allow to create, delete and change tag names and descriptions locally."
    override val category: AppPermissionCategory = AppPermissionCategory.WRITE

}