package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object ModelReadPermission : AppPermission {
    override val key = AppPermissionKey("model_read")
    override val name: String = "Models: read"
    override val description: String = "Allow actor to read existing models."
    override val category: AppPermissionCategory = AppPermissionCategory.READ
}