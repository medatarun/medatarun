package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission

object TagGlobalManagePermission : AppPermission {
    override val key: String = "tag_global_manage"
    override val name: String = "Tags: manage global tags"
    override val description: String = "Allow user to create, delete and change global tags."
}