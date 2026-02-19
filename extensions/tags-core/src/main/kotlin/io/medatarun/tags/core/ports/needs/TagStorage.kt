package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.TagManagedId
import io.medatarun.tags.core.domain.TagManagedKey
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagFree
import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagManaged
import io.medatarun.tags.core.internal.TagGroupInMemory

interface TagStorage {
    fun findTagFreeByKeyOptional(key: TagFreeKey): TagFree?
    fun findTagFreeByIdOptional(id: TagFreeId): TagFree?

    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup?
    fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup?

    fun findTagManagedByIdOptional(id: TagManagedId): TagManaged?
    fun findTagManagedByKeyOptional(id: TagGroupId, key: TagManagedKey): TagManaged?

    fun dispatch(cmd: TagRepoCmd)

}
