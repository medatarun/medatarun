package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object ModelDeletePermission: AppPermission {
    override val key = AppPermissionKey("model_delete")
    override val name: String = "Models: delete"
    override val description: String = "Allow actor to completely delete a model."
    override val implies = listOf(ModelReadPermission.key)
    override val category: AppPermissionCategory = AppPermissionCategory.WRITE
}