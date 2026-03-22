package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPrincipalRole

object TagLocalManageRole : AppPrincipalRole {
    override val key: String
        get() = "tag_local_manage"
}