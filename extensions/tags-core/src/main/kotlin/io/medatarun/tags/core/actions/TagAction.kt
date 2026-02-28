package io.medatarun.tags.core.actions

import io.medatarun.actions.actions.ActionUILocation
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.security.SecurityRuleNames
import io.medatarun.tags.core.domain.*

sealed interface TagAction {
    // ------------------------------------------------------------------------
    // Free tag
    // ------------------------------------------------------------------------
    @ActionDoc(
        key = "tag_free_create",
        title = "Create a free tag",
        description = "Creates a new free tag, with key, name and description.",
        uiLocations = [TagActionUILocation.tag_free_list],
        securityRule = TagSecurityRuleNames.TAG_FREE_MANAGE
    )
    data class TagFreeCreate(
        val scopeRef: TagScopeRef, val key: TagKey, val name: String?, val description: String?
    ) : TagAction

    @ActionDoc(
        key = "tag_free_update_name",
        title = "Update free tag name",
        description = "Updates the name of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRuleNames.TAG_FREE_MANAGE
    )
    class TagFreeUpdateName(val tagRef: TagRef, val value: String?) : TagAction

    @ActionDoc(
        key = "tag_free_update_description",
        title = "Update free tag description",
        description = "Updates the description of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRuleNames.TAG_FREE_MANAGE
    )
    class TagFreeUpdateDescription(val tagRef: TagRef, val value: String?) : TagAction

    @ActionDoc(
        key = "tag_free_update_key",
        title = "Update free tag key",
        description = "Updates the key of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRuleNames.TAG_FREE_MANAGE
    )
    class TagFreeUpdateKey(val tagRef: TagRef, val value: TagKey) : TagAction

    @ActionDoc(
        key = "tag_free_delete",
        title = "Delete free tag",
        description = "Delete a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRuleNames.TAG_FREE_MANAGE
    )
    class TagFreeDelete(val tagRef: TagRef) : TagAction

    // ------------------------------------------------------------------------
    // ManagedTagGroup
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_group_create",
        title = "Create a managed tag group",
        description = "Creates a new managed tag group, with key, name and description.",
        uiLocations = [TagActionUILocation.tag_managed_group_list],
        securityRule = TagSecurityRuleNames.TAG_GROUP_MANAGE
    )
    class TagGroupCreate(val key: TagGroupKey, val name: String?, val description: String?) : TagAction

    @ActionDoc(
        key = "tag_group_update_name",
        title = "Update managed tag group name",
        description = "Updates the name of a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagGroupUpdateName(val tagGroupRef: TagGroupRef, val value: String) : TagAction

    @ActionDoc(
        key = "tag_group_update_description",
        title = "Update managed tag group description",
        description = "Updates the description of a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagGroupUpdateDescription(val tagGroupRef: TagGroupRef, val value: String) : TagAction

    @ActionDoc(
        key = "tag_group_update_key",
        title = "Update managed tag group key",
        description = "Updates the key of a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagGroupUpdateKey(val tagGroupRef: TagGroupRef, val value: TagGroupKey) : TagAction

    @ActionDoc(
        key = "tag_group_delete",
        title = "Delete managed tag group",
        description = "Deletes a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagGroupDelete(val tagGroupRef: TagGroupRef) : TagAction

    // ------------------------------------------------------------------------
    // ManagedTag
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_managed_create",
        title = "Create a managed tag",
        description = "Creates a new managed tag, in a group, with key, name and description.",
        uiLocations = [TagActionUILocation.tag_managed_list],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagManagedCreate(val groupRef: TagGroupRef, val key: TagKey, val name: String?, val description: String?) : TagAction

    @ActionDoc(
        key = "tag_managed_update_name",
        title = "Update managed tag name",
        description = "Updates the name of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateName(val tagRef: TagRef, val value: String) : TagAction

    @ActionDoc(
        key = "tag_managed_update_description",
        title = "Update managed tag description",
        description = "Updates the description of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateDescription(val tagRef: TagRef, val value: String) : TagAction

    @ActionDoc(
        key = "tag_managed_update_key",
        title = "Update managed tag key",
        description = "Updates the key of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateKey(val tagRef: TagRef, val value: TagKey) : TagAction

    @ActionDoc(
        key = "tag_managed_delete",
        title = "Delete managed tag",
        description = "Deletes a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRuleNames.TAG_MANAGED_MANAGE
    )
    class TagManagedDelete(val tagRef: TagRef) : TagAction

    // -----------------------------------------------------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_search",
        title = "Tag search",
        description = "Searches known tags. Without filters, returns all tags. Use filters to restrict the result, for example to one scope such as global tags or the local tags of one scope.",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class TagSearch(
        @ActionParamDoc(
            name="filters",
            description = ""
        )
        val filters: TagSearchFilters?) : TagAction

    @ActionDoc(
        key = "tag_group_list",
        title = "Tag group list",
        description = "List all group of managed tags.",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class TagGroupList() : TagAction
}
