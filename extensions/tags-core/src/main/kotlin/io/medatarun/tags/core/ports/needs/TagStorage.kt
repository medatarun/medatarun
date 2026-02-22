package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.*

interface TagStorage {

    fun findAllTag():List<Tag>
    fun findAllTagGroup():List<TagGroup>

    fun findTagByKeyOptional(groupId: TagGroupId?, key: TagKey): Tag?
    fun findTagByIdOptional(id: TagId): Tag?

    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup?
    fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup?

    fun dispatch(cmd: TagRepoCmd)


}
