package io.medatarun.model.security

import io.medatarun.security.AppPermission

object ModelUpdateAuthorityPermission: AppPermission {
    override val key: String = "model_update_authority"
    override val name: String = "Models: change autority"
    override val description: String = "Allow actor to change model authority (from system to canonical)."
    override val implies: List<AppPermission> = listOf(ModelReadPermission)
}