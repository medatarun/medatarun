package io.medatarun.tags.core.ports.needs

interface TagScopeManagerResolver {
    fun findScopeManagers(): List<TagScopeManager>
}
