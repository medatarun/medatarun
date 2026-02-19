package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagManagedId
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagManaged
import io.medatarun.tags.core.domain.TagManagedKey

data class TagManagedInMemory(
    override val id: TagManagedId,
    override val key: TagManagedKey,
    override val groupId: TagGroupId,
    override val name: String?,
    override val description: String?
) : TagManaged {
}