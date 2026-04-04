package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission

object TagGlobalManagePermission : AppPermission {
    override val key: String = "tag_global_manage"
}