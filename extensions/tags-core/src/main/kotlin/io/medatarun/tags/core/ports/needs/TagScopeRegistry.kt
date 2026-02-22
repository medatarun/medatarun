package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.TagScopeType

interface TagScopeRegistry {
    fun findManagerByScopeType(type: TagScopeType): TagScopeManager?
    fun findAllManagers(): List<TagScopeManager>
}
