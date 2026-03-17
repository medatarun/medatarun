package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.domain.diff.ModelDiff
import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.model.domain.search.*
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.*
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagRef
import java.text.Collator
import java.text.Normalizer
import java.util.*

class ModelQueriesImpl(
    private val storage: ModelStorage,
    private val tagResolver: ModelTagResolver
) : ModelQueries {
    private val diffRunner = ModelDiffRunner()

    override fun findAllModelIds(): List<ModelId> {
        return storage.findAllModelIds()
    }

    override fun findAllModelSummaries(locale: Locale): List<ModelSummary> {
        val textComparator = TextComparator(locale)
        val modelIds = storage.findAllModelIds()
        return modelIds.map { id ->
            try {
                val model = findModelById(id)
                ModelSummary(
                    id = model.id,
                    key = model.key,
                    name = model.name?.get(locale),
                    description = model.description?.get(locale),
                    authority = model.authority,
                    error = null,
                    countTypes = model.types.size,
                    countEntities = model.entities.size,
                    countRelationships = model.relationships.size
                )
            } catch (e: Exception) {
                ModelSummary(
                    id = id,
                    key = ModelKey.generateRandom(),
                    name = null,
                    description = null,
                    authority = ModelAuthority.SYSTEM,
                    error = e.message,
                    countTypes = 0, countEntities = 0, countRelationships = 0
                )
            }
        }.sortedWith(
            Comparator.comparing(
                { it.name ?: it.key.value },
                Comparator.nullsLast(textComparator)
            )
        )
    }

    override fun findEntity(
        modelRef: ModelRef,
        entityRef: EntityRef
    ): Entity {
        val model = findModel(modelRef)
        return model.findEntityOptional(entityRef) ?: throw EntityNotFoundException(modelRef, entityRef)
    }

    override fun findEntityAttributeOptional(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeRef: EntityAttributeRef
    ): Attribute? {
        val model = findModel(modelRef)
        return model.findEntityAttributeOptional(entityRef, attributeRef)
    }

    override fun findEntityAttribute(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeRef: EntityAttributeRef
    ): Attribute {
        return findEntityAttributeOptional(modelRef, entityRef, attributeRef)
            ?: throw EntityAttributeNotFoundException(modelRef, entityRef, attributeRef)
    }

    override fun findType(
        modelRef: ModelRef,
        typeRef: TypeRef
    ): ModelType {
        return findModel(modelRef).findTypeOptional(typeRef) ?: throw TypeNotFoundException(modelRef, typeRef)
    }

    override fun diff(leftModelRef: ModelRef, rightModelRef: ModelRef, scope: ModelDiffScope): ModelDiff {
        val leftModel = findModel(leftModelRef)
        val rightModel = findModel(rightModelRef)
        return diffRunner.diff(leftModel, rightModel, scope)
    }

    override fun findModelByKey(modelKey: ModelKey): ModelAggregate {
        return storage.findModelAggregateByKeyOptional(modelKey)
            ?: throw ModelNotFoundByKeyException(modelKey)
    }

    override fun findModelById(modelId: ModelId): ModelAggregate {
        return storage.findModelAggregateByIdOptional(modelId)
            ?: throw ModelNotFoundByIdException(modelId)
    }

    override fun findModel(modelRef: ModelRef): ModelAggregate {
        return when (modelRef) {
            is ModelRef.ById -> findModelById(modelRef.id)
            is ModelRef.ByKey -> findModelByKey(modelRef.key)
        }
    }

    override fun findModelAtVersion(modelRef: ModelRef, modelVersion: ModelVersion): ModelAggregate {
        val model = storage.findModel(modelRef)
        return storage.findModelAggregateVersion(model.id, modelVersion)
    }

    override fun findModelVersions(modelRef: ModelRef): List<ModelChangeEvent> {
        val model = storage.findModel(modelRef)
        return storage.findModelVersions(model.id)
    }

    override fun findModelChangeEventsSinceVersion(
        modelRef: ModelRef,
        modelVersion: ModelVersion
    ): List<ModelChangeEvent> {
        val model = storage.findModel(modelRef)
        return storage.findModelChangeEventsSinceVersion(model.id, modelVersion)
    }

    override fun findModelOptional(modelRef: ModelRef): ModelAggregate? {
        return when (modelRef) {
            is ModelRef.ById -> storage.findModelAggregateByIdOptional(modelRef.id)
            is ModelRef.ByKey -> storage.findModelAggregateByKeyOptional(modelRef.key)
        }
    }

    private class TextComparator(locale: Locale) : Comparator<String> {
        private val collator = Collator.getInstance(locale)
        private val comparator = Comparator.nullsLast(
            Comparator<String> { x, y ->
                val xnfd = Normalizer.normalize(x, Normalizer.Form.NFD)
                val ynfd = Normalizer.normalize(y, Normalizer.Form.NFD)
                norm(xnfd, ynfd)
            }
        )

        fun norm(x: String, y: String): Int {
            return collator.compare(x, y)
        }

        override fun compare(o1: String?, o2: String?): Int {
            return comparator.compare(o1, o2)

        }
    }

    override fun search(query: SearchQuery): SearchResults {

        // Transforms the SearchQuery into a storage search query.
        // We need to resolve tag refs into tag ids.

        val collectedTagRef = query.filters.items.flatMap {
            when (it) {
                is SearchFilterTags.AllOf -> it.names
                is SearchFilterTags.AnyOf -> it.names
                is SearchFilterTags.NoneOf -> it.names
                else -> emptyList()
            }
        }.associateWith { tagResolver.resolveTagIdUnsafe(it) }

        fun toTagId(tagRef: TagRef): TagId {
            return collectedTagRef[tagRef] ?: throw ModelQuerySearchCouldNotResolveTagRef(tagRef)
        }

        fun toTagIds(tagRefs: List<TagRef>): List<TagId> {
            // Please note the distinct here, which is a business rule that says:
            // if the same tag is given twice, we need to deduplicate (there are unit tests about this)
            return tagRefs.map { toTagId(it) }.distinct()
        }

        fun toStorageSearchFilter(filter: SearchFilter): ModelStorageSearchFilter {
            val storageFilter: ModelStorageSearchFilter = when (filter) {
                is SearchFilterText.Contains -> ModelStorageSearchFilterText.Contains(filter.value)
                is SearchFilterTags.AllOf -> ModelStorageSearchFilterTags.AllOf(toTagIds(filter.names))
                is SearchFilterTags.AnyOf -> ModelStorageSearchFilterTags.AnyOf(toTagIds(filter.names))
                SearchFilterTags.Empty -> ModelStorageSearchFilterTags.Empty
                is SearchFilterTags.NoneOf -> ModelStorageSearchFilterTags.NoneOf(toTagIds(filter.names))
                SearchFilterTags.NotEmpty -> ModelStorageSearchFilterTags.NotEmpty
            }
            return storageFilter
        }

        val storageQuery = ModelStorageSearchQuery(
            filters = ModelStorageSearchFilters(
                operator = query.filters.operator,
                items = query.filters.items.map { filter -> toStorageSearchFilter(filter) }
            ),
            fields = query.fields
        )
        return storage.search(storageQuery)
    }

}
