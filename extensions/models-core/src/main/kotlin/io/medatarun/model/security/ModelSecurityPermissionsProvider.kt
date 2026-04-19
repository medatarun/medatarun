package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.SecurityPermissionsProvider

class ModelSecurityPermissionsProvider : SecurityPermissionsProvider {
    val list = listOf<AppPermission>(
        ModelCopyPermission,
        ModelCreatePermission,
        ModelDeletePermission,
        ModelImportPermission,
        ModelReadPermission,
        ModelReleasePermission,
        ModelUpdateAuthorityPermission,
        ModelWritePermission
    )

    override fun getPermissions(): List<AppPermission> {
        return list
    }
}