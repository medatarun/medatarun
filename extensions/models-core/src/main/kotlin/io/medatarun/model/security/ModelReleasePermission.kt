package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelReleasePermission: AppPermission {
    override val key: String = "model_release"
    override val name: String = "Models: release version"
    override val description: String = "Allow actor to release a version of a model."
    override val implies: List<AppPermission> = listOf(ModelReadPermission)
}