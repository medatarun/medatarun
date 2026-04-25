package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object ModelReleasePermission: AppPermission {
    override val key = AppPermissionKey( "model_release")
    override val name: String = "Models: release version"
    override val description: String = "Allow actor to release a version of a model."
    override val implies = listOf(ModelReadPermission.key)
    override val category: AppPermissionCategory  = AppPermissionCategory.WRITE
}