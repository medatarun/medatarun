package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.ports.needs.TagStorage

class TagQueriesImpl(private val storage: TagStorage): TagQueries {
    private val tagRefResolver = TagRefResolver(storage)

    override fun findAllTags(): List<Tag> {
        return storage.findAllTag()
    }

    override fun findAllTagGroup(): List<TagGroup> {
        return storage.findAllTagGroup()
    }

    override fun findTagByRefOptional(tagRef: TagRef): Tag? {
        return tagRefResolver.findTagByRefOptional(tagRef)
    }

    override fun findTagByRef(tagRef: TagRef): Tag {
        return findTagByRefOptional(tagRef) ?: throw TagNotFoundException(tagRef.asString())
    }
}
