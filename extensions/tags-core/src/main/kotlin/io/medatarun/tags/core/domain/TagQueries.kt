package io.medatarun.tags.core.domain

interface TagQueries {
    fun findAllFreeTags(): List<TagFree>
    fun findAllManagedTags(): List<TagManaged>
    fun findAllManagedTaggGroup(): List<TagGroup>
}