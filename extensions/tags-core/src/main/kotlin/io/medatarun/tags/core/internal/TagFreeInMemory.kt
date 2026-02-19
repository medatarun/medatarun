package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagFree
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagFreeId
import javax.swing.text.html.HTML

data class TagFreeInMemory(
    override val id: TagFreeId,
    override val key: TagFreeKey,
    override val name: String?,
    override val description: String?
) : TagFree {
    companion object {
        fun of(other: TagFree): TagFreeInMemory {
            return TagFreeInMemory(
                id = other.id, key = other.key, name = other.name, description = other.description
            )
        }
    }
}