package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPrincipalRole

object TagFreeManageRole : AppPrincipalRole {
    override val key: String
        get() = "tag_free_manage"
}