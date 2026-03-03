package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPrincipalRole

object TagManagedManageRole : AppPrincipalRole {
    override val key: String
        get() = "tag_managed_manage"
}