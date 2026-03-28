package io.medatarun.tags.core.domain

import java.time.Instant

interface TagQueries {
    fun findAllTags(): List<Tag>


    /**
     * Searches known tags using the provided filters.
     *
     * Contract:
     * - if [TagSearchFilters.items] in [query] is empty, it means "no restrictions" and returns all known tags.
     *   This keeps the caller contract simple: action/UI code can omit filters without having
     *   to build a dedicated "match all" filter.
     * - filters are combined using [TagSearchFilters.operator]
     * - a local `scopeRef` filter is validated before filtering
     * - if a local `scopeRef` type has no registered manager, search fails with [TagScopeManagerNotFoundException]
     * - if a local `scopeRef` does not exist, search fails with [TagScopeNotFoundException]
     * - this is the query contract used by `TagAction.TagSearch`
     *
     * Search is intentionally built as a list of predicates so new filter kinds can be added without moving
     * search semantics back into action-layer code.
     *
     * The goal is to keep search semantics here instead of spreading filter logic
     * across action providers.
     */
    fun search(query: TagSearchFilters): List<Tag>

    /**
     * Returns all tags from this scope. Returns an empty list if scope doesn't exist
     */
    fun findAllTagByScopeRef(scopeRef: TagScopeRef): List<Tag>

    fun findAllTagGroup(): List<TagGroup>

    fun findTagByRefOptional(tagRef: TagRef): Tag?
    fun findTagByRef(tagRef: TagRef): Tag


    fun findTagByKeyOptional(id: TagGroupId, tagKey: TagKey): Tag?
    fun findTagGroupByKeyOptional(groupKey: TagGroupKey): TagGroup?
    fun findTagByIdOptional(id: TagId): Tag?
    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup?
    fun findTagByIdAsOfOptional(tagId: TagId, eventDate: Instant): Tag?
    fun findTagGroupByIdAsOfOptional(tagGroupId: TagGroupId, eventDate: Instant): TagGroup?
}
