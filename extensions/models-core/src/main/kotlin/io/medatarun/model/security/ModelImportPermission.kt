package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelImportPermission: AppPermission {
    override val key: String = "model_import"
    override val name: String = "Models: import"
    override val description: String = "Allow actor to import models using the import features."
    override val implies: List<AppPermission> = listOf(ModelReadPermission)
}