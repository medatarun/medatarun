package io.medatarun.tags.core.domain

interface TagQueries {
    fun findAllTags(): List<Tag>
    fun search(filters: TagSearchFilters): List<Tag>
    fun findAllTagGroup(): List<TagGroup>

    fun findTagByRefOptional(tagRef: TagRef): Tag?
    fun findTagByRef(tagRef: TagRef): Tag


    fun findTagByKeyOptional(id: TagGroupId, managedKey: TagKey): Tag?
    fun findTagGroupByKeyOptional(groupKey: TagGroupKey):TagGroup?
    fun findTagByIdOptional(id: TagId): Tag?
    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup?
}
