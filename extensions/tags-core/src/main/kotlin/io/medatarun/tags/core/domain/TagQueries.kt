package io.medatarun.tags.core.domain

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
     * - this is the query contract used by `TagAction.TagSearch`
     *
     * Search is intentionally built as a list of predicates so new filter kinds can be added without moving
     * search semantics back into action-layer code.
     *
     * The goal is to keep search semantics here instead of spreading filter logic
     * across action providers.
     */
    fun search(query: TagSearchFilters): List<Tag>
    fun findAllTagGroup(): List<TagGroup>

    fun findTagByRefOptional(tagRef: TagRef): Tag?
    fun findTagByRef(tagRef: TagRef): Tag


    fun findTagByKeyOptional(id: TagGroupId, managedKey: TagKey): Tag?
    fun findTagGroupByKeyOptional(groupKey: TagGroupKey):TagGroup?
    fun findTagByIdOptional(id: TagId): Tag?
    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup?
}
