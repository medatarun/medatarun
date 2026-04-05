package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission

object TagLocalManagePermission : AppPermission {
    override val key: String = "tag_local_manage"
    override val name: String = "Tags: manage local tags"
    override val description: String = "Allow to create, delete and change tag names and descriptions locally."

}