package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagScope

data class TagInMemory(
    override val id: TagId,
    override val key: TagKey,
    override val scope: TagScope,
    override val groupId: TagGroupId?,
    override val name: String?,
    override val description: String?
) : Tag {
    companion object {
        fun of(other: Tag): TagInMemory {
            return TagInMemory(
                id = other.id,
                key = other.key,
                scope = other.scope,
                groupId = other.groupId,
                name = other.name,
                description = other.description
            )
        }
    }
}
