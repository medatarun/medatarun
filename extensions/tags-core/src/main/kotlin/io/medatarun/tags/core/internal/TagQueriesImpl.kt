package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagNotFoundException
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.ports.needs.TagStorage

class TagQueriesImpl(private val storage: TagStorage): TagQueries {
    override fun findAllTags(): List<Tag> {
        return storage.findAllTag()
    }

    override fun findAllTagGroup(): List<TagGroup> {
        return storage.findAllTagGroup()
    }

    override fun findTagByRefOptional(tagRef: TagRef): Tag? {
        return when(tagRef) {
            is TagRef.ById -> storage.findTagByIdOptional(tagRef.id)
            is TagRef.ByKey -> {
                val groupKey = tagRef.groupKey
                if (groupKey == null) {
                    storage.findTagByKeyOptional(null, tagRef.key)
                } else {
                    val group = storage.findTagGroupByKeyOptional(groupKey) ?: return null
                    storage.findTagByKeyOptional(group.id, tagRef.key)
                }
            }
        }
    }

    override fun findTagByRef(tagRef: TagRef): Tag {
        return findTagByRefOptional(tagRef) ?: throw TagNotFoundException(tagRef.asString())
    }
}
