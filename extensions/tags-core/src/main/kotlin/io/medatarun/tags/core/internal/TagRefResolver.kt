package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.ports.needs.TagStorage

/**
 * Resolves a tag reference into a tag entity using the unified storage API.
 * The ByKey path still needs a group-key to group-id lookup before reading tags.
 */
class TagRefResolver(private val storage: TagStorage) {
    fun findTagByRefOptional(tagRef: TagRef): Tag? {
        return when (tagRef) {
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
}
