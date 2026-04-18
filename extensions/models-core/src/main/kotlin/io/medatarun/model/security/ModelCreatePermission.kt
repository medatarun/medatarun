package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelCreatePermission: AppPermission {
    override val key: String = "model_create_manual"
    override val name: String = "Models: create manually"
    override val description: String = "Allow actor to manually create a model."
    override val implies: List<AppPermission> = listOf(ModelReadPermission, ModelWritePermission)

}