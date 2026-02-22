package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey

sealed interface TagRepoCmd {

    data class TagCreate(val item: Tag) : TagRepoCmd
    data class TagUpdateKey(val tagId: TagId, val value: TagKey) : TagRepoCmd
    data class TagUpdateName(val tagId: TagId, val value: String?) : TagRepoCmd
    data class TagUpdateDescription(val tagId: TagId, val value: String?) : TagRepoCmd
    data class TagDelete(val tagId: TagId) : TagRepoCmd

    data class TagGroupCreate(val item: TagGroup) : TagRepoCmd
    data class TagGroupUpdateKey(val tagGroupId: TagGroupId, val value: TagGroupKey) : TagRepoCmd
    data class TagGroupUpdateName(val tagGroupId: TagGroupId, val value: String?) : TagRepoCmd
    data class TagGroupUpdateDescription(val tagGroupId: TagGroupId, val value: String?) : TagRepoCmd
    data class TagGroupDelete(val tagGroupId: TagGroupId) : TagRepoCmd
}
