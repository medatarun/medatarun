package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission

object TagGroupManagePermission : AppPermission {
    override val key: String = "tag_group_manage"
    override val name: String = "Tags: manage groups"
    override val description: String = "Allow to create, delete and change groups of tags."

}