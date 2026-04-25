package io.medatarun.model.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionCategory
import io.medatarun.security.AppPermissionKey

object ModelImportPermission: AppPermission {
    override val key = AppPermissionKey( "model_import")
    override val name: String = "Models: import"
    override val description: String = "Allow actor to import models using the import features."
    override val implies = listOf(ModelReadPermission.key)
    override val category: AppPermissionCategory  = AppPermissionCategory.WRITE
}