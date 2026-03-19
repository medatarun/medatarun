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
        description = "Creates a tag that can be used directly in a scope, without belonging to a managed group.",
        uiLocations = [TagActionUILocation.tag_free_list],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    data class TagFreeCreate(
        @ActionParamDoc(
            order = 10,
            name = "Scope",
            description = "Scope where this tag will be available."
        )
        val scopeRef: TagScopeRef,
        @ActionParamDoc(
            order = 30,
            name = "Key",
            description = """
                Stable business code used to identify this tag in this scope.
                
                Use only letters, digits, `_` and `-`.
                
                Example: `customer-visible`
            """
        )
        val key: TagKey,
        @ActionParamDoc(
            order = 20,
            name = "Name",
            description = """
                Name of this tag.
            """
        )
        val name: String?,
        @ActionParamDoc(
            order = 40,
            name = "Description",
            description = """
                Optional help text shown to users.
                
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
    class TagFreeUpdateName(
        @ActionParamDoc(
            order = 10,
            name = "Tag",
            description = "Tag to update."
        )
        val tagRef: TagRef,
        @ActionParamDoc(
            order = 20,
            name = "Name",
            description = "Name of this tag."
        )
        val value: String?
    ) : TagAction

    @ActionDoc(
        key = "tag_free_update_description",
        title = "Update free tag description",
        description = "Updates the description of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    class TagFreeUpdateDescription(
        @ActionParamDoc(
            order = 10,
            name = "Tag",
            description = "Tag to update."
        )
        val tagRef: TagRef,
        @ActionParamDoc(
            order = 20,
            name = "Description",
            description = "Explain what this tag means and when it should be used."
        )
        val value: String?
    ) : TagAction

    @ActionDoc(
        key = "tag_free_update_key",
        title = "Update free tag key",
        description = "Updates the key of a free tag.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    class TagFreeUpdateKey(
        @ActionParamDoc(
            order = 10,
            name = "Tag",
            description = "Tag to update."
        )
        val tagRef: TagRef,
        @ActionParamDoc(
            order = 20,
            name = "Key",
            description = """
                Stable business code used to identify this tag in its scope.
                
                Use only letters, digits, `_` and `-`.
            """
        )
        val value: TagKey
    ) : TagAction

    @ActionDoc(
        key = "tag_free_delete",
        title = "Delete free tag",
        description = "Deletes a free tag from its scope.",
        uiLocations = [TagActionUILocation.tag_free_detail],
        securityRule = TagSecurityRules.TAG_FREE_MANAGE
    )
    class TagFreeDelete(
        @ActionParamDoc(
            order = 10,
            name = "Tag",
            description = "Tag to delete."
        )
        val tagRef: TagRef
    ) : TagAction

    // ------------------------------------------------------------------------
    // ManagedTagGroup
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_group_create",
        title = "Create a managed tag group",
        description = "Creates a group used to organize managed tags that belong together.",
        uiLocations = [TagActionUILocation.tag_managed_group_list],
        securityRule = TagSecurityRules.TAG_GROUP_MANAGE
    )
    class TagGroupCreate(
        @ActionParamDoc(
            name = "Key",
            description = """
                Stable business code used to identify this group.
                
                It must be unique across all groups.
            """,
            order = 30,
        )
        val key: TagGroupKey,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this group.",
            order = 20,
        )
        val name: String?,
        @ActionParamDoc(
            name = "Description",
            description = "Explain what this group is for and which tags belong in it.",
            order = 40,
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
            description = "Group to update.",
            order = 10,
        )
        val tagGroupRef: TagGroupRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this group.",
            order = 20,
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
            description = "Group to update.",
            order = 10,
        )
        val tagGroupRef: TagGroupRef,
        @ActionParamDoc(
            name = "Description",
            description = "Explain what this group is for and which tags belong in it.",
            order = 20,
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
            description = "Group to update.",
            order = 10,
        )
        val tagGroupRef: TagGroupRef,
        @ActionParamDoc(
            name = "Key",
            description = """
                Stable business code used to identify this group.
                
                It must be unique across all groups.
            """,
            order = 20,
        )
        val value: TagGroupKey) : TagAction

    @ActionDoc(
        key = "tag_group_delete",
        title = "Delete managed tag group",
        description = "Deletes a managed tag group.",
        uiLocations = [TagActionUILocation.tag_managed_group_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagGroupDelete(
        @ActionParamDoc(
            name = "Tag group",
            description = "Group to delete.",
            order = 10,
        )
        val tagGroupRef: TagGroupRef
    ) : TagAction

    // ------------------------------------------------------------------------
    // ManagedTag
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_managed_create",
        title = "Create a managed tag",
        description = "Creates a tag inside a managed group.",
        uiLocations = [TagActionUILocation.tag_managed_list],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedCreate(
        @ActionParamDoc(
            name = "Group",
            description = "Group that will contain this tag.",
            order = 10,
        )
        val groupRef: TagGroupRef,
        @ActionParamDoc(
            name = "Key",
            description = """
                Stable business code used to identify this tag inside its group.
                
                Use only letters, digits, `_` and `-`.
            """,
            order = 30,
        )
        val key: TagKey,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this tag.",
            order = 20,
        )
        val name: String?,
        @ActionParamDoc(
            name = "Description",
            description = "Explain what this tag means and when it should be used.",
            order = 40,
        )
        val description: String?
    ) : TagAction

    @ActionDoc(
        key = "tag_managed_update_name",
        title = "Update managed tag name",
        description = "Updates the name of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateName(
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to update.",
            order = 10,
        )
        val tagRef: TagRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this tag.",
            order = 20,
        )
        val value: String
    ) : TagAction

    @ActionDoc(
        key = "tag_managed_update_description",
        title = "Update managed tag description",
        description = "Updates the description of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateDescription(
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to update.",
            order = 10,
        )
        val tagRef: TagRef,
        @ActionParamDoc(
            name = "Description",
            description = "Explain what this tag means and when it should be used.",
            order = 20,
        )
        val value: String
    ) : TagAction

    @ActionDoc(
        key = "tag_managed_update_key",
        title = "Update managed tag key",
        description = "Updates the key of a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedUpdateKey(
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to update.",
            order = 10,
        )
        val tagRef: TagRef,
        @ActionParamDoc(
            name = "Key",
            description = """
                Stable business code used to identify this tag inside its group.
                
                Use only letters, digits, `_` and `-`.
            """,
            order = 20,
        )
        val value: TagKey
    ) : TagAction

    @ActionDoc(
        key = "tag_managed_delete",
        title = "Delete managed tag",
        description = "Deletes a managed tag.",
        uiLocations = [TagActionUILocation.tag_managed_detail],
        securityRule = TagSecurityRules.TAG_MANAGED_MANAGE
    )
    class TagManagedDelete(
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to delete.",
            order = 10,
        )
        val tagRef: TagRef
    ) : TagAction

    // -----------------------------------------------------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_search",
        title = "Tag search",
        description = "Searches known tags. Without filters, returns all tags. Use filters to narrow the result.",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class TagSearch(
        @ActionParamDoc(
            name = "Filters",
            description = "Optional filters used to narrow the search result.",
            order = 10,
        )
        val filters: TagSearchFilters?
    ) : TagAction

    @ActionDoc(
        key = "tag_group_list",
        title = "Tag group list",
        description = "Lists all managed tag groups.",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class TagGroupList : TagAction
}
