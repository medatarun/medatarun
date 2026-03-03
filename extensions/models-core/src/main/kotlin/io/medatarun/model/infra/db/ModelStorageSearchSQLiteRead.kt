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
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagRef
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.notInSubQuery
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ModelStorageSearchSQLiteRead(
    private val dbConnectionFactory: DbConnectionFactory
) {
        fun search(query: SearchQuery, tagResolver: ModelTagResolver): SearchResults {
            return dbConnectionFactory.withExposed {
                val matchingIds = resolveMatchingSearchItemIds(query, tagResolver)
                if (matchingIds.isEmpty()) {
                    return@withExposed SearchResults(emptyList())
                }

                val rows = ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.selectAll()
                    .where { ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id inList matchingIds.toList() }
                    .orderBy(
                        ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.attributeLabel to SortOrder.ASC,
                        ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipLabel to SortOrder.ASC,
                        ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityLabel to SortOrder.ASC,
                        ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelLabel to SortOrder.ASC
                    )
                    .toList()

                SearchResults(
                    rows.map { row ->
                        SearchResultItem(
                            id = row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id],
                            location = searchLocationFromRow(row),
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
                        ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                            .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                            .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
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
            return ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                .where {
                    ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id notInSubQuery
                            ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable
                                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId)
                }
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                .toSet()
        }

        private fun selectSearchItemIdsWithTags(): Set<String> {
            return ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId)
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId] }
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

            return ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId)
                .where { ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.tagId inList tagIds }
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId] }
                .toSet()
        }

        private fun selectSearchItemIdsWithNoneOfTags(
            filter: SearchFilterTags.NoneOf,
            tagResolver: ModelTagResolver
        ): Set<String> {
            val tagIds = resolveTagIds(filter.names, tagResolver)
            if (tagIds.isEmpty()) {
                return ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                    .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                    .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                    .toSet()
            }

            return ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                .where {
                    ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id notInSubQuery
                            ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable
                                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId)
                                .where { ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.tagId inList tagIds }
                }
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                .toSet()
        }

        private fun selectSearchItemIdsWithAllTags(
            filter: SearchFilterTags.AllOf,
            tagResolver: ModelTagResolver
        ): Set<String> {
            val tagIds = resolveTagIds(filter.names, tagResolver)
            if (tagIds.isEmpty()) {
                return ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                    .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                    .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                    .toSet()
            }

            val rows = ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.selectAll()
                .where { ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.tagId inList tagIds }
                .toList()

            return rows
                .groupBy { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId] }
                .filterValues { itemRows ->
                    itemRows.map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.tagId] }
                        .distinct()
                        .size == tagIds.size
                }
                .keys
        }

        private fun selectSearchItemIdsByContains(filter: SearchFilterText.Contains): Set<String> {
            val searchedText = normalizeModelSearchText(filter.value)
            if (searchedText.isBlank()) {
                return ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                    .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                    .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                    .toSet()
            }

            val pattern = "%" + escapeLikePattern(searchedText) + "%"
            return ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                .where { ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.searchText like pattern }
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                .toSet()
        }

        private fun resolveTagIds(
            tagRefs: List<TagRef>,
            tagResolver: ModelTagResolver
        ): List<String> {
            return tagRefs.map { tagResolver.resolveTagId(it).asString() }.distinct()
        }

        private fun searchLocationFromRow(row: ResultRow): DomainLocation {
            return when (row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.itemType]) {
                SearchItemType.MODEL.code -> {
                    ModelLocation(
                        id = ModelId.Companion.fromString(row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelId]),
                        key = ModelKey(row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelKey]),
                        label = row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelLabel]
                    )
                }

                SearchItemType.ENTITY.code -> {
                    EntityLocation(
                        model = modelLocationFromRow(row),
                        id = EntityId.Companion.fromString(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityId
                            )
                        ),
                        key = EntityKey(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityKey
                            )
                        ),
                        label = requiredSearchRowValue(
                            row,
                            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityLabel
                        )
                    )
                }

                SearchItemType.ENTITY_ATTRIBUTE.code -> {
                    EntityAttributeLocation(
                        entity = EntityLocation(
                            model = modelLocationFromRow(row),
                            id = EntityId.Companion.fromString(
                                requiredSearchRowValue(
                                    row,
                                    ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityId
                                )
                            ),
                            key = EntityKey(
                                requiredSearchRowValue(
                                    row,
                                    ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityKey
                                )
                            ),
                            label = requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityLabel
                            )
                        ),
                        id = AttributeId.Companion.fromString(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.attributeId
                            )
                        ),
                        key = AttributeKey(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.attributeKey
                            )
                        ),
                        label = requiredSearchRowValue(
                            row,
                            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.attributeLabel
                        )
                    )
                }

                SearchItemType.RELATIONSHIP.code -> {
                    RelationshipLocation(
                        model = modelLocationFromRow(row),
                        id = RelationshipId.Companion.fromString(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipId
                            )
                        ),
                        key = RelationshipKey(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipKey
                            )
                        ),
                        label = requiredSearchRowValue(
                            row,
                            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipLabel
                        )
                    )
                }

                SearchItemType.RELATIONSHIP_ATTRIBUTE.code -> {
                    RelationshipAttributeLocation(
                        relationship = RelationshipLocation(
                            model = modelLocationFromRow(row),
                            id = RelationshipId.Companion.fromString(
                                requiredSearchRowValue(
                                    row,
                                    ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipId
                                )
                            ),
                            key = RelationshipKey(
                                requiredSearchRowValue(
                                    row,
                                    ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipKey
                                )
                            ),
                            label = requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipLabel
                            )
                        ),
                        id = AttributeId.Companion.fromString(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.attributeId
                            )
                        ),
                        key = AttributeKey(
                            requiredSearchRowValue(
                                row,
                                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.attributeKey
                            )
                        ),
                        label = requiredSearchRowValue(
                            row,
                            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.attributeLabel
                        )
                    )
                }

                else -> throw ModelStorageSearchSQLiteUnknownItemTypeException(
                    row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.itemType]
                )
            }
        }

        private fun modelLocationFromRow(row: ResultRow): ModelLocation {
            return ModelLocation(
                id = ModelId.Companion.fromString(row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelId]),
                key = ModelKey(row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelKey]),
                label = row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelLabel]
            )
        }

        private fun requiredSearchRowValue(row: ResultRow, column: Column<String?>): String {
            return row[column] ?: throw ModelStorageSearchSQLiteMissingProjectionReferenceException(column.name)
        }

        private fun escapeLikePattern(value: String): String {
            return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
        }
    }