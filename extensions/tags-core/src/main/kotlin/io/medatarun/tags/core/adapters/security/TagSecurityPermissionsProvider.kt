package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission
import io.medatarun.security.SecurityPermissionsProvider

class TagSecurityPermissionsProvider : SecurityPermissionsProvider {
    override fun getPermissions(): List<AppPermission> {
        return listOf(TagLocalManagePermission, TagGlobalManagePermission, TagGroupManagePermission)
    }
    override fun getRenamedPermissions():Map<String, String> {
        return mapOf(
            "tag_free_manage" to TagLocalManagePermission.key,
            "tag_managed_manage" to TagGlobalManagePermission.key
        )
    }
}