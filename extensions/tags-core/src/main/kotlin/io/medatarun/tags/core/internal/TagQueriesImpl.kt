package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagFreeRef
import io.medatarun.tags.core.domain.TagFree
import io.medatarun.tags.core.domain.TagFreeNotFoundException
import io.medatarun.tags.core.domain.TagManaged
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.ports.needs.TagStorage

class TagQueriesImpl(private val storage: TagStorage): TagQueries {
    override fun findAllFreeTags(): List<TagFree> {
        return storage.findAllTagFree()
    }

    override fun findAllManagedTags(): List<TagManaged> {
        return storage.findAllTagManaged()
    }

    override fun findAllTagGroup(): List<TagGroup> {
        return storage.findAllTagGroup()
    }

    override fun findTagFreeByRefOptional(tagRef: TagFreeRef): TagFree? {
        return when(tagRef) {
            is TagFreeRef.ById -> storage.findTagFreeByIdOptional(tagRef.id)
            is TagFreeRef.ByKey -> storage.findTagFreeByKeyOptional(tagRef.key)
        }
    }
    override fun findTagFreeByRef(tagRef: TagFreeRef): TagFree {
        return findTagFreeByRefOptional(tagRef) ?: throw TagFreeNotFoundException(tagRef.asString())
    }
}