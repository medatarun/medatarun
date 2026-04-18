package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelReadPermission: AppPermission {
    override val key: String = "model_read"
    override val name: String = "Models: read"
    override val description: String = "Allow actor to read existing models."
}