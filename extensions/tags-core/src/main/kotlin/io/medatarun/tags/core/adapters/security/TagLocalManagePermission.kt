package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission

object TagLocalManagePermission : AppPermission {
    override val key: String = "tag_local_manage"
}