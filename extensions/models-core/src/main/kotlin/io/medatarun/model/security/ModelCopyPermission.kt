package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelCopyPermission: AppPermission {
    override val key: String = "model_copy"
    override val name: String = "Models: copy"
    override val description: String = "Allow actor to copy a model as a new one."
    override val implies: List<AppPermission> = listOf(ModelReadPermission)
}