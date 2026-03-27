package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRolesProvider

class TagSecurityRolesProvider : SecurityRolesProvider {
    override fun getRoles(): List<AppPrincipalRole> {
        return listOf(TagLocalManageRole, TagGlobalManageRole, TagGroupManageRole)
    }
    override fun getRenamedRoles():Map<String, String> {
        return mapOf(
            "tag_free_manage" to TagLocalManageRole.key,
            "tag_managed_manage" to TagGlobalManageRole.key
        )
    }
}