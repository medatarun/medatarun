package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine

data class TagGroupInMemory(
    override val id: TagGroupId,
    override val key: TagGroupKey,
    override val name: TextSingleLine?,
    override val description: TextMarkdown?
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