package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPrincipalRole

object TagGroupManageRole : AppPrincipalRole {
    override val key: String
        get() = "tag_group_manage"
}