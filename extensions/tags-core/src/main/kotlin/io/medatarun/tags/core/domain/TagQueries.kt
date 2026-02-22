package io.medatarun.tags.core.domain

interface TagQueries {
    fun findAllTags(): List<Tag>
    fun findAllTagGroup(): List<TagGroup>

    fun findTagByRefOptional(tagRef: TagRef): Tag?
    fun findTagByRef(tagRef: TagRef): Tag
}
