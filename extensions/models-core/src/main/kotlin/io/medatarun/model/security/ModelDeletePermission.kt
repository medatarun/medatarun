package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelDeletePermission: AppPermission {
    override val key: String = "model_delete"
    override val name: String = "Models: delete"
    override val description: String = "Allow actor to completely delete a model."
    override val implies: List<AppPermission> = listOf(ModelReadPermission)

}