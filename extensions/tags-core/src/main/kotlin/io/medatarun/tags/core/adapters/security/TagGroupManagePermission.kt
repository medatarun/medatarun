package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object TagGroupManagePermission : AppPermission {
    override val key = AppPermissionKey("tag_group_manage")
    override val name: String = "Tags: manage groups"
    override val description: String = "Allow to create, delete and change groups of tags."
    override val category: AppPermissionCategory = AppPermissionCategory.WRITE

}