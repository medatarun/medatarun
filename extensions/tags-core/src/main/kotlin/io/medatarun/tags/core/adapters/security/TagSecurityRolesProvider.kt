package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRolesProvider

class TagSecurityRolesProvider : SecurityRolesProvider {
    override fun getRoles(): List<AppPrincipalRole> {
        return listOf(TagLocalManageRole, TagGlobalManageRole, TagGroupManageRole)
    }
}