package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object ModelWritePermission: AppPermission {
    override val key = AppPermissionKey("model_write")
    override val name: String = "Models: write"
    override val description: String = "Allow actor to update model contents."
    override val implies = listOf(ModelReadPermission.key)
    override val category: AppPermissionCategory = AppPermissionCategory.WRITE

}