package io.medatarun.model.infra.db

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.DomainLocation
import io.medatarun.model.domain.EntityAttributeLocation
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityLocation
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelLocation
import io.medatarun.model.domain.RelationshipAttributeLocation
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipLocation
import io.medatarun.model.domain.search.SearchFilter
import io.medatarun.model.domain.search.SearchFilterTags
import io.medatarun.model.domain.search.SearchFilterText
import io.medatarun.model.domain.search.SearchFiltersLogicalOperator
import io.medatarun.model.domain.search.SearchQuery
import io.medatarun.model.domain.search.SearchResultItem
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.domain.search.normalizeModelSearchText
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTable
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTagTable
import io.medatarun.model.infra.db.records.DenormModelSearchItemRecord
import io.medatarun.model.infra.db.records.DenormModelSearchItemTagRecord
import io.medatarun.model.infra.db.records.SearchItemType
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagRef
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.notInSubQuery
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ModelStorageDbSearchRead(
    private val dbConnectionFactory: DbConnectionFactory
) {
        fun search(query: SearchQuery, tagResolver: ModelTagResolver): SearchResults {
            return dbConnectionFactory.withExposed {
                val matchingIds = resolveMatchingSearchItemIds(query, tagResolver)
                if (matchingIds.isEmpty()) {
                    return@withExposed SearchResults(emptyList())
                }

                val rows = DenormModelSearchItemTable.selectAll()
                    .where { DenormModelSearchItemTable.id inList matchingIds.toList() }
                    .orderBy(
                        DenormModelSearchItemTable.attributeLabel to SortOrder.ASC,
                        DenormModelSearchItemTable.relationshipLabel to SortOrder.ASC,
                        DenormModelSearchItemTable.entityLabel to SortOrder.ASC,
                        DenormModelSearchItemTable.modelLabel to SortOrder.ASC
                    )
                    .toList()
                    .map { DenormModelSearchItemRecord.read(it) }

                SearchResults(
                    rows.map { item ->
                        SearchResultItem(
                            id = item.id,
                            location = searchLocationFromRecord(item),
                            fields = emptyMap()
                        )
                    }
                )
            }
        }

        private fun resolveMatchingSearchItemIds(query: SearchQuery, tagResolver: ModelTagResolver): Set<String> {
            if (query.filters.items.isEmpty()) {
                return when (query.filters.operator) {
                    SearchFiltersLogicalOperator.AND -> {
                        DenormModelSearchItemTable
                            .select(DenormModelSearchItemTable.id)
                            .map { it[DenormModelSearchItemTable.id] }
                            .toSet()
                    }

                    SearchFiltersLogicalOperator.OR -> emptySet()
                }
            }

            val filterMatches = query.filters.items.map { resolveFilterMatchIds(it, tagResolver) }
            return when (query.filters.operator) {
                SearchFiltersLogicalOperator.AND -> {
                    val first = filterMatches.first()
                    filterMatches.drop(1).fold(first) { acc, current -> acc.intersect(current) }
                }

                SearchFiltersLogicalOperator.OR -> {
                    filterMatches.fold(emptySet()) { acc, current -> acc.union(current) }
                }
            }
        }

        private fun resolveFilterMatchIds(filter: SearchFilter, tagResolver: ModelTagResolver): Set<String> {
            return when (filter) {
                is SearchFilterTags.Empty -> selectSearchItemIdsWithoutTags()
                is SearchFilterTags.NotEmpty -> selectSearchItemIdsWithTags()
                is SearchFilterTags.AnyOf -> selectSearchItemIdsWithAnyTag(filter, tagResolver)
                is SearchFilterTags.NoneOf -> selectSearchItemIdsWithNoneOfTags(filter, tagResolver)
                is SearchFilterTags.AllOf -> selectSearchItemIdsWithAllTags(filter, tagResolver)
                is SearchFilterText.Contains -> selectSearchItemIdsByContains(filter)
            }
        }

        private fun selectSearchItemIdsWithoutTags(): Set<String> {
            return DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .where {
                    DenormModelSearchItemTable.id notInSubQuery
                            DenormModelSearchItemTagTable
                                .select(DenormModelSearchItemTagTable.searchItemId)
                }
                .map { it[DenormModelSearchItemTable.id] }
                .toSet()
        }

        private fun selectSearchItemIdsWithTags(): Set<String> {
            return DenormModelSearchItemTagTable
                .select(DenormModelSearchItemTagTable.searchItemId)
                .map { it[DenormModelSearchItemTagTable.searchItemId] }
                .toSet()
        }

        private fun selectSearchItemIdsWithAnyTag(
            filter: SearchFilterTags.AnyOf,
            tagResolver: ModelTagResolver
        ): Set<String> {
            val tagIds = resolveTagIds(filter.names, tagResolver)
            if (tagIds.isEmpty()) {
                return emptySet()
            }

            return DenormModelSearchItemTagTable
                .select(DenormModelSearchItemTagTable.searchItemId)
                .where { DenormModelSearchItemTagTable.tagId inList tagIds }
                .map { it[DenormModelSearchItemTagTable.searchItemId] }
                .toSet()
        }

        private fun selectSearchItemIdsWithNoneOfTags(
            filter: SearchFilterTags.NoneOf,
            tagResolver: ModelTagResolver
        ): Set<String> {
            val tagIds = resolveTagIds(filter.names, tagResolver)
            if (tagIds.isEmpty()) {
                return DenormModelSearchItemTable
                    .select(DenormModelSearchItemTable.id)
                    .map { it[DenormModelSearchItemTable.id] }
                    .toSet()
            }

            return DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .where {
                    DenormModelSearchItemTable.id notInSubQuery
                            DenormModelSearchItemTagTable
                                .select(DenormModelSearchItemTagTable.searchItemId)
                                .where { DenormModelSearchItemTagTable.tagId inList tagIds }
                }
                .map { it[DenormModelSearchItemTable.id] }
                .toSet()
        }

        private fun selectSearchItemIdsWithAllTags(
            filter: SearchFilterTags.AllOf,
            tagResolver: ModelTagResolver
        ): Set<String> {
            val tagIds = resolveTagIds(filter.names, tagResolver)
            if (tagIds.isEmpty()) {
                return DenormModelSearchItemTable
                    .select(DenormModelSearchItemTable.id)
                    .map { it[DenormModelSearchItemTable.id] }
                    .toSet()
            }

            val rows = DenormModelSearchItemTagTable.selectAll()
                .where { DenormModelSearchItemTagTable.tagId inList tagIds }
                .toList()
                .map { DenormModelSearchItemTagRecord.read(it) }

            return rows
                .groupBy { it.searchItemId }
                .filterValues { itemRows ->
                    itemRows.map { it.tagId }
                        .distinct()
                        .size == tagIds.size
                }
                .keys
        }

        private fun selectSearchItemIdsByContains(filter: SearchFilterText.Contains): Set<String> {
            val searchedText = normalizeModelSearchText(filter.value)
            if (searchedText.isBlank()) {
                return DenormModelSearchItemTable
                    .select(DenormModelSearchItemTable.id)
                    .map { it[DenormModelSearchItemTable.id] }
                    .toSet()
            }

            val pattern = "%" + escapeLikePattern(searchedText) + "%"
            return DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .where { DenormModelSearchItemTable.searchText like pattern }
                .map { it[DenormModelSearchItemTable.id] }
                .toSet()
        }

        private fun resolveTagIds(
            tagRefs: List<TagRef>,
            tagResolver: ModelTagResolver
        ): List<String> {
            return tagRefs.map { tagResolver.resolveTagId(it).asString() }.distinct()
        }

        private fun searchLocationFromRecord(item: DenormModelSearchItemRecord): DomainLocation {
            return when (item.itemType) {
                SearchItemType.MODEL -> {
                    ModelLocation(
                        id = ModelId.Companion.fromString(item.modelId),
                        key = ModelKey(item.modelKey),
                        label = item.modelLabel
                    )
                }

                SearchItemType.ENTITY -> {
                    EntityLocation(
                        model = modelLocationFromRecord(item),
                        id = EntityId.Companion.fromString(requiredValue(item.entityId, "entity_id")),
                        key = EntityKey(requiredValue(item.entityKey, "entity_key")),
                        label = requiredValue(item.entityLabel, "entity_label")
                    )
                }

                SearchItemType.ENTITY_ATTRIBUTE -> {
                    EntityAttributeLocation(
                        entity = EntityLocation(
                            model = modelLocationFromRecord(item),
                            id = EntityId.Companion.fromString(requiredValue(item.entityId, "entity_id")),
                            key = EntityKey(requiredValue(item.entityKey, "entity_key")),
                            label = requiredValue(item.entityLabel, "entity_label")
                        ),
                        id = AttributeId.Companion.fromString(requiredValue(item.attributeId, "attribute_id")),
                        key = AttributeKey(requiredValue(item.attributeKey, "attribute_key")),
                        label = requiredValue(item.attributeLabel, "attribute_label")
                    )
                }

                SearchItemType.RELATIONSHIP -> {
                    RelationshipLocation(
                        model = modelLocationFromRecord(item),
                        id = RelationshipId.Companion.fromString(requiredValue(item.relationshipId, "relationship_id")),
                        key = RelationshipKey(requiredValue(item.relationshipKey, "relationship_key")),
                        label = requiredValue(item.relationshipLabel, "relationship_label")
                    )
                }

                SearchItemType.RELATIONSHIP_ATTRIBUTE -> {
                    RelationshipAttributeLocation(
                        relationship = RelationshipLocation(
                            model = modelLocationFromRecord(item),
                            id = RelationshipId.Companion.fromString(requiredValue(item.relationshipId, "relationship_id")),
                            key = RelationshipKey(requiredValue(item.relationshipKey, "relationship_key")),
                            label = requiredValue(item.relationshipLabel, "relationship_label")
                        ),
                        id = AttributeId.Companion.fromString(requiredValue(item.attributeId, "attribute_id")),
                        key = AttributeKey(requiredValue(item.attributeKey, "attribute_key")),
                        label = requiredValue(item.attributeLabel, "attribute_label")
                    )
                }


            }
        }

        private fun modelLocationFromRecord(item: DenormModelSearchItemRecord): ModelLocation {
            return ModelLocation(
                id = ModelId.Companion.fromString(item.modelId),
                key = ModelKey(item.modelKey),
                label = item.modelLabel
            )
        }

        private fun requiredValue(value: String?, columnName: String): String {
            return value ?: throw ModelStorageDbSearchMissingProjectionReferenceException(columnName)
        }

        private fun escapeLikePattern(value: String): String {
            return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
        }
    }
