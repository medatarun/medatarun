package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelWritePermission: AppPermission {
    override val key: String = "model_write"
    override val name: String = "Models: write"
    override val description: String = "Allow actor to update model contents."
    override val implies: List<AppPermission> = listOf(ModelReadPermission)
}