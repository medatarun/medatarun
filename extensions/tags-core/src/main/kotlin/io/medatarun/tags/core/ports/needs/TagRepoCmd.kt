package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.TagFree
import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagManaged
import io.medatarun.tags.core.domain.TagManagedId
import io.medatarun.tags.core.domain.TagManagedKey

sealed interface TagRepoCmd {

    data class TagFreeCreate(val item: TagFree) : TagRepoCmd
    data class TagFreeUpdateKey(val tagFreeId: TagFreeId, val value: TagFreeKey) : TagRepoCmd
    data class TagFreeUpdateName(val tagFreeId: TagFreeId, val value: String?) : TagRepoCmd
    data class TagFreeUpdateDescription(val tagFreeId: TagFreeId, val value: String?) : TagRepoCmd
    data class TagFreeDelete(val tagFreeId: TagFreeId) : TagRepoCmd

    data class TagGroupCreate(val item: TagGroup) : TagRepoCmd
    data class TagGroupUpdateKey(val tagGroupId: TagGroupId, val value: TagGroupKey) : TagRepoCmd
    data class TagGroupUpdateName(val tagGroupId: TagGroupId, val value: String?) : TagRepoCmd
    data class TagGroupUpdateDescription(val tagGroupId: TagGroupId, val value: String?) : TagRepoCmd
    data class TagGroupDelete(val tagGroupId: TagGroupId) : TagRepoCmd

    data class TagManagedCreate(val item: TagManaged) : TagRepoCmd
    data class TagManagedUpdateKey(val tagManagedId: TagManagedId, val value: TagManagedKey) : TagRepoCmd
    data class TagManagedUpdateName(val tagManagedId: TagManagedId, val value: String?) : TagRepoCmd
    data class TagManagedUpdateDescription(val tagManagedId: TagManagedId, val value: String?) : TagRepoCmd
    data class TagManagedDelete(val tagManagedId: TagManagedId) : TagRepoCmd
}