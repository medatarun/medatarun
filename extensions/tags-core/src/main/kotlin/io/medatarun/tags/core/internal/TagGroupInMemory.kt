package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey

data class TagGroupInMemory(
    override val id: TagGroupId,
    override val key: TagGroupKey,
    override val name: String?,
    override val description: String?
): TagGroup {
    companion object {
        fun of(other: TagGroup): TagGroupInMemory {
                return TagGroupInMemory(
                    id = other.id,
                    key = other.key,
                    name = other.name,
                    description = other.description

                )
        }
    }
}