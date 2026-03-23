package io.medatarun.tags.core.ports.needs

import io.medatarun.storage.eventsourcing.StorageCmd
import io.medatarun.tags.core.domain.*

sealed interface TagStorageCmd: StorageCmd {

    data class TagCreate(val item: Tag) : TagStorageCmd
    data class TagUpdateKey(val tagId: TagId, val value: TagKey) : TagStorageCmd
    data class TagUpdateName(val tagId: TagId, val value: String?) : TagStorageCmd
    data class TagUpdateDescription(val tagId: TagId, val value: String?) : TagStorageCmd
    data class TagDelete(val tagId: TagId) : TagStorageCmd

    data class TagGroupCreate(val item: TagGroup) : TagStorageCmd
    data class TagGroupUpdateKey(val tagGroupId: TagGroupId, val value: TagGroupKey) : TagStorageCmd
    data class TagGroupUpdateName(val tagGroupId: TagGroupId, val value: String?) : TagStorageCmd
    data class TagGroupUpdateDescription(val tagGroupId: TagGroupId, val value: String?) : TagStorageCmd
    data class TagGroupDelete(val tagGroupId: TagGroupId) : TagStorageCmd
}
