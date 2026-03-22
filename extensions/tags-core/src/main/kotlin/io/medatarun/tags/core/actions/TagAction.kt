package io.medatarun.tags.core.actions

import io.medatarun.actions.actions.ActionUILocation
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.security.SecurityRuleNames
import io.medatarun.tags.core.adapters.security.TagSecurityRules
import io.medatarun.tags.core.domain.*

sealed interface TagAction {

    // ------------------------------------------------------------------------
    // Local tags
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_local_create",
        title = "Create a local tag",
        description = "Creates a tag, local to a scope (a model for example), without belonging to a group.",
        uiLocations = [TagActionUILocation.tag_local_list],
        securityRule = TagSecurityRules.TAG_LOCAL_MANAGE
    )
    data class TagLocalCreate(
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
        key = "tag_local_update_name",
        title = "Update local tag name",
        description = "Updates the name of a local tag.",
        uiLocations = [TagActionUILocation.tag_local_detail],
        securityRule = TagSecurityRules.TAG_LOCAL_MANAGE
    )
    class TagLocalUpdateName(
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
        key = "tag_local_update_description",
        title = "Update local tag description",
        description = "Updates the description of a local tag.",
        uiLocations = [TagActionUILocation.tag_local_detail],
        securityRule = TagSecurityRules.TAG_LOCAL_MANAGE
    )
    class TagLocalUpdateDescription(
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
        key = "tag_local_update_key",
        title = "Update local tag key",
        description = "Updates the key of a local tag.",
        uiLocations = [TagActionUILocation.tag_local_detail],
        securityRule = TagSecurityRules.TAG_LOCAL_MANAGE
    )
    class TagLocalUpdateKey(
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
        key = "tag_local_delete",
        title = "Delete local tag",
        description = "Deletes a local tag from its scope.",
        uiLocations = [TagActionUILocation.tag_local_detail],
        securityRule = TagSecurityRules.TAG_LOCAL_MANAGE
    )
    class TagLocalDelete(
        @ActionParamDoc(
            order = 10,
            name = "Tag",
            description = "Tag to delete."
        )
        val tagRef: TagRef
    ) : TagAction

    // ------------------------------------------------------------------------
    // Tag Groups
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_group_create",
        title = "Create a tag group",
        description = "Creates a group used to organize global tags that belong together.",
        uiLocations = [TagActionUILocation.tag_group_list],
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
        val description: String?
    ) : TagAction

    @ActionDoc(
        key = "tag_group_update_name",
        title = "Update global tag group name",
        description = "Updates the name of a tag group.",
        uiLocations = [TagActionUILocation.tag_group_detail],
        securityRule = TagSecurityRules.TAG_GROUP_MANAGE
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
        val value: String
    ) : TagAction

    @ActionDoc(
        key = "tag_group_update_description",
        title = "Update tag group description",
        description = "Updates the description of a tag group.",
        uiLocations = [TagActionUILocation.tag_group_detail],
        securityRule = TagSecurityRules.TAG_GROUP_MANAGE
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
        val value: String
    ) : TagAction

    @ActionDoc(
        key = "tag_group_update_key",
        title = "Update tag group key",
        description = "Updates the key of a tag group.",
        uiLocations = [TagActionUILocation.tag_group_detail],
        securityRule = TagSecurityRules.TAG_GROUP_MANAGE
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
        val value: TagGroupKey
    ) : TagAction

    @ActionDoc(
        key = "tag_group_delete",
        title = "Delete tag group",
        description = "Deletes a tag group.",
        uiLocations = [TagActionUILocation.tag_group_detail],
        securityRule = TagSecurityRules.TAG_GROUP_MANAGE
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
    // Global Tags
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "tag_global_create",
        title = "Create a global tag",
        description = "Creates a tag inside a group.",
        uiLocations = [TagActionUILocation.tag_global_list],
        securityRule = TagSecurityRules.TAG_GLOBAL_MANAGE
    )
    class TagGlobalCreate(
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
        key = "tag_global_update_name",
        title = "Update global tag name",
        description = "Updates the name of a global tag.",
        uiLocations = [TagActionUILocation.tag_global_detail],
        securityRule = TagSecurityRules.TAG_GLOBAL_MANAGE
    )
    class TagGlobalUpdateName(
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
        key = "tag_global_update_description",
        title = "Update global tag description",
        description = "Updates the description of a global tag.",
        uiLocations = [TagActionUILocation.tag_global_detail],
        securityRule = TagSecurityRules.TAG_GLOBAL_MANAGE
    )
    class TagGlobalUpdateDescription(
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
        key = "tag_global_update_key",
        title = "Update global tag key",
        description = "Updates the key of a global tag.",
        uiLocations = [TagActionUILocation.tag_global_detail],
        securityRule = TagSecurityRules.TAG_GLOBAL_MANAGE
    )
    class TagGlobalUpdateKey(
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
        key = "tag_global_delete",
        title = "Delete global tag",
        description = "Deletes a global tag.",
        uiLocations = [TagActionUILocation.tag_global_detail],
        securityRule = TagSecurityRules.TAG_GLOBAL_MANAGE
    )
    class TagGlobalDelete(
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
        description = "Lists all tag groups.",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class TagGroupList : TagAction
}
