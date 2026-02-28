package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.ports.needs.TagStorage

class TagQueriesImpl(private val storage: TagStorage): TagQueries {
    private val tagRefResolver = TagRefResolver(storage)

    override fun findAllTags(): List<Tag> {
        return storage.findAllTag()
    }

    override fun search(query: TagSearchFilters): List<Tag> {

        if (query.items.isEmpty()) {
            return findAllTags()
        }

        val predicates = query.items.map { filter ->
            when (filter) {
                is TagSearchFilterScopeRef.Is -> {
                    { tag: Tag -> tag.scope == filter.value }
                }
            }
        }
        val items = findAllTags()
        return when (query.operator) {
            TagSearchFiltersLogicalOperator.AND -> items.filter { item ->
                predicates.all { predicate -> predicate(item) }
            }
            TagSearchFiltersLogicalOperator.OR -> items.filter { item ->
                predicates.any { predicate -> predicate(item) }
            }
        }
    }

    override fun findAllTagGroup(): List<TagGroup> {
        return storage.findAllTagGroup()
    }

    override fun findTagByRefOptional(tagRef: TagRef): Tag? {
        return tagRefResolver.findTagByRefOptional(tagRef)
    }

    override fun findTagByRef(tagRef: TagRef): Tag {
        return findTagByRefOptional(tagRef) ?: throw TagNotFoundException(tagRef.asString())
    }

    override fun findTagByKeyOptional(id: TagGroupId, managedKey: TagKey): Tag? {
        return storage.findTagByKeyOptional(id, managedKey)
    }

    override fun findTagGroupByKeyOptional(groupKey: TagGroupKey): TagGroup? {
        return storage.findTagGroupByKeyOptional(groupKey)
    }

    override fun findTagByIdOptional(id: TagId): Tag? {
        return storage.findTagByIdOptional(id)
    }

    override fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        return storage.findTagGroupByIdOptional(id)
    }
}
