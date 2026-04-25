package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object ModelCopyPermission : AppPermission {
    override val key = AppPermissionKey("model_copy")
    override val name: String = "Models: copy"
    override val description: String = "Allow actor to copy a model as a new one."
    override val implies: List<AppPermissionKey> = listOf(ModelReadPermission.key)
    override val category: AppPermissionCategory = AppPermissionCategory.WRITE
}