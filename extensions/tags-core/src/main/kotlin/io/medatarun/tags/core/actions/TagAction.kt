package io.medatarun.tags.core.actions

import io.medatarun.actions.actions.ActionUILocation
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.security.SecurityRuleNames
import io.medatarun.tags.core.adapters.security.TagSecurityRules
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
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    data class TagFreeCreate(
        @ActionParamDoc(
            order = 0,
            name = "Scope",
            description = "Local scope that will own this tag."
        )
        val scopeRef: TagScopeRef,
        @ActionParamDoc(
            order = 1,
            name = "Key",
            description = """
                Unique key of the tag in this scope.
                
                Use only letters, digits, `_` and `-`.
                
                Example: `customer-visible`
            """
        )
        val key: TagKey,
        @ActionParamDoc(
            order = 2,
            name = "Name",
            description = """
                Optional label shown to users.
                
                Use it when the key is too technical to read directly.
            """
        )
        val name: String?,
        @ActionParamDoc(
            order = 3,
            name = "Description",
            description = """
                Optional help text for users.
                
                Explain what the tag means and when it should be used.
            """
        )
        val description: String?
    ) : TagAction

    @ActionDoc(
        key = "tag_free_update_name",
        title = "Update free tag name",
        description = "Updates the name of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    class TagFreeUpdateName(val tagRef: TagRef, val value: String?) : TagAction

    @ActionDoc(
        key = "tag_free_update_description",
        title = "Update free tag description",
        description = "Updates the description of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    class TagFreeUpdateDescription(val tagRef: TagRef, val value: String?) : TagAction

    @ActionDoc(
        key = "tag_free_update_key",
        title = "Update free tag key",
        description = "Updates the key of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    class TagFreeUpdateKey(val tagRef: TagRef, val value: TagKey) : TagAction

    @ActionDoc(
        key = "tag_free_delete",
        title = "Delete free tag",
        description = "Delete a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    class TagFreeDelete(
        @ActionParamDoc(
            name = "Tag",
            description = "Reference of the tag to delete."
        )
        val tagRef: TagRef
    ) : TagAction

    // ------------------------------------------------------------------------
    // ManagedTagGroup
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_group_create",
        title = "Create a managed tag group",
        description = "Creates a new managed tag group, with key, name and description.",
        uiLocations = [TagActionUILocation.tag_managed_group_list],
        securityRule = TagSecurityRules.TAG_GROUP_MANAGE
    )
    class TagGroupCreate(
        @ActionParamDoc(
            name = "Name",
            description = "Name of the tag.",
            order = 1,
        )
        val name: String?,
        @ActionParamDoc(
            name = "Key",
            description = "Unique key for this group. Keys must be unique across all groups.",
            order = 2,
        )
        val key: TagGroupKey,
        @ActionParamDoc(
            name = "Description",
            description = "Description of usages and meaning of this group, what kind of tags it can contain.",
            order = 3,
        )
        val description: String?) : TagAction

    @ActionDoc(
        key = "tag_group_update_name",
        title = "Update managed tag group name",
        description = "Updates the name of a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagGroupUpdateName(
        @ActionParamDoc(
            name = "Tag group",
            description = "Reference of the group to update",
            order = 1,
        )
        val tagGroupRef: TagGroupRef,
        @ActionParamDoc(
            name = "Name",
            description = "New name of the group",
            order = 2,
        )
        val value: String) : TagAction

    @ActionDoc(
        key = "tag_group_update_description",
        title = "Update managed tag group description",
        description = "Updates the description of a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagGroupUpdateDescription(
        @ActionParamDoc(
            name = "Tag group",
            description = "Reference of the group to update",
            order = 1,
        )
        val tagGroupRef: TagGroupRef,
        @ActionParamDoc(
            name = "Description",
            description = "Description of usages and meaning of this group, what kind of tags it can contain.",
            order = 2,
        )
        val value: String) : TagAction

    @ActionDoc(
        key = "tag_group_update_key",
        title = "Update managed tag group key",
        description = "Updates the key of a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagGroupUpdateKey(
        @ActionParamDoc(
            name = "Tag group",
            description = "Reference of the group to update",
            order = 1,
        )
        val tagGroupRef: TagGroupRef,
        @ActionParamDoc(
            name = "Key",
            description = "New group key. Must be unique across all groups.",
            order = 2,
        )
        val value: TagGroupKey) : TagAction

    @ActionDoc(
        key = "tag_group_delete",
        title = "Delete managed tag group",
        description = "Deletes a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
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
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedCreate(val groupRef: TagGroupRef, val key: TagKey, val name: String?, val description: String?) :
        TagAction

    @ActionDoc(
        key = "tag_managed_update_name",
        title = "Update managed tag name",
        description = "Updates the name of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateName(val tagRef: TagRef, val value: String) : TagAction

    @ActionDoc(
        key = "tag_managed_update_description",
        title = "Update managed tag description",
        description = "Updates the description of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateDescription(val tagRef: TagRef, val value: String) : TagAction

    @ActionDoc(
        key = "tag_managed_update_key",
        title = "Update managed tag key",
        description = "Updates the key of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateKey(val tagRef: TagRef, val value: TagKey) : TagAction

    @ActionDoc(
        key = "tag_managed_delete",
        title = "Delete managed tag",
        description = "Deletes a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
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
            name = "filters",
            description = ""
        )
        val filters: TagSearchFilters?
    ) : TagAction

    @ActionDoc(
        key = "tag_group_list",
        title = "Tag group list",
        description = "List all group of managed tags.",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class TagGroupList() : TagAction
}
