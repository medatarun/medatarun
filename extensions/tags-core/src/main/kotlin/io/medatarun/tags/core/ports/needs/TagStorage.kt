package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.*
import java.time.Instant

interface TagStorage {

    fun findAllTag():List<Tag>
    fun findAllTagByScopeRef(scopeRef: TagScopeRef): List<Tag>
    fun search(query: TagSearchFilters): List<Tag>
    fun findAllTagGroup():List<TagGroup>

    fun findTagByKeyOptional(scope: TagScopeRef, groupId: TagGroupId?, key: TagKey): Tag?
    fun findTagByKeyOptional(groupId: TagGroupId?, key: TagKey): Tag? {
        return findTagByKeyOptional(TagScopeRef.Global, groupId, key)
    }
    fun findTagByIdOptional(id: TagId): Tag?
    fun findTagByIdAsOfOptional(id: TagId, eventDate: Instant): Tag?

    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup?
    fun findTagGroupByIdAsOfOptional(id: TagGroupId, eventDate: Instant): TagGroup?
    fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup?

    /**
     * Process one tag storage command.
     *
     * The storage layer uses the traceability record to keep write-side effects
     * aligned with the originating action.
     */
    fun dispatch(cmdEnv: TagStorageCmdEnveloppe)


}
