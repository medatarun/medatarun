package io.medatarun.model.internal

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.*
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelStorages
import java.text.Collator
import java.text.Normalizer
import java.util.*

class ModelQueriesImpl(private val storage: ModelStorages) : ModelQueries {

    override fun findAllModelIds(): List<ModelId> {
        return storage.findAllModelIds()
    }

    override fun findAllModelSummaries(locale: Locale): List<ModelSummary> {
        val textComparator = TextComparator(locale)
        return storage.findAllModelIds().map { id ->
            try {
                val model = storage.findModelById(id)
                ModelSummary(
                    id = model.id,
                    key = model.key,
                    name = model.name?.get(locale),
                    description = model.description?.get(locale),
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

    override fun findModelByKey(modelKey: ModelKey): Model {
        return storage.findModelByKeyOptional(modelKey) ?: throw ModelNotFoundByKeyException(modelKey)
    }

    override fun findModelById(modelId: ModelId): Model {
        return storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundByIdException(modelId)
    }

    override fun findModel(modelRef: ModelRef): Model {
        return when (modelRef) {
            is ModelRef.ById -> findModelById(modelRef.id)
            is ModelRef.ByKey -> findModelByKey(modelRef.key)
        }
    }

    override fun findModelOptional(modelRef: ModelRef): Model? {
        return when (modelRef) {
            is ModelRef.ById -> storage.findModelByIdOptional(modelRef.id)
            is ModelRef.ByKey -> storage.findModelByKeyOptional(modelRef.key)
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
        val index = QueryIndexBuilder(storage).build()
        fun toQueryItemPredicate(filter: SearchFilter): (QueryIndexItem) -> Boolean {
            return { indexedItem ->
                when (filter) {
                    is SearchFilterTags.Empty -> {
                        indexedItem.tags.isEmpty()
                    }

                    is SearchFilterTags.NotEmpty -> {
                        indexedItem.tags.isNotEmpty()
                    }

                    is SearchFilterTags.AllOf -> {
                        val searchedTags = filter.names.map { tagName -> Hashtag(tagName) }
                        searchedTags.all(indexedItem.tags::contains)
                    }

                    is SearchFilterTags.NoneOf -> {
                        val searchedTags = filter.names.map { tagName -> Hashtag(tagName) }
                        searchedTags.none { indexedItem.tags.contains(it) }
                    }

                    is SearchFilterTags.AnyOf -> {
                        val searchedTags = filter.names.map { tagName -> Hashtag(tagName) }
                        searchedTags.any { indexedItem.tags.contains(it) }
                    }
                }
            }
        }

        val predicateChain = query.filters.filters.map { toQueryItemPredicate(it) }
        val filteredItems = when (query.filters.operator) {
            SearchFiltersLogicalOperator.AND -> {
                index.items.filter {
                    predicateChain.all { pred -> pred(it) }
                }
            }

            SearchFiltersLogicalOperator.OR -> {
                index.items.filter {
                    predicateChain.any { pred -> pred(it) }
                }
            }
        }
        return SearchResults(
            filteredItems.map {
                SearchResultItem(
                    id = UuidUtils.generateV7().toString(),
                    location = it.location,
                    fields = emptyMap()
                )
            }
        )
    }

    class QueryIndex(val items: List<QueryIndexItem>)
    class QueryIndexItem(val location: DomainLocation, val tags: List<Hashtag>)

    class QueryIndexBuilder(private val storage: ModelStorages) {
        fun build(): QueryIndex {
            val index = mutableListOf<QueryIndexItem>()
            storage.findAllModelIds().forEach { modelId ->
                val model = storage.findModelById(modelId)
                index.add(QueryIndexItem(createModelLocation(model), model.hashtags))
                model.entities.forEach { entity ->
                    index.add(QueryIndexItem(createEntityLocation(model, entity), entity.hashtags))
                    entity.attributes.forEach { attr ->
                        index.add(QueryIndexItem(createEntityAttributeLocation(model, entity, attr), attr.hashtags))
                    }
                }
                model.relationships.forEach { rel ->
                    index.add(QueryIndexItem(createRelationshipLocation(model, rel), rel.hashtags))
                    rel.attributes.forEach { attr ->
                        index.add(QueryIndexItem(createRelationshipAttributeLocation(model, rel, attr), attr.hashtags))
                    }
                }
            }
            return QueryIndex(index)
        }
    }


}

fun createModelLocation(model: Model): ModelLocation {
    return ModelLocation(model.id, model.key, model.name?.name ?: model.key.value)
}

fun createEntityLocation(model: Model, entity: Entity): EntityLocation {
    return EntityLocation(
        createModelLocation(model),
        id = entity.id,
        key = entity.key,
        label = entity.name?.name ?: entity.key.value
    )
}

fun createEntityAttributeLocation(model: Model, entity: Entity, attr: Attribute): EntityAttributeLocation {
    return EntityAttributeLocation(
        createEntityLocation(model, entity),
        id = attr.id,
        key = attr.key,
        label = attr.name?.name ?: attr.key.value
    )
}

fun createRelationshipLocation(model: Model, rel: Relationship): RelationshipLocation {
    return RelationshipLocation(
        createModelLocation(model),
        id = rel.id,
        key = rel.key,
        label = rel.name?.name ?: rel.key.value
    )
}

fun createRelationshipAttributeLocation(
    model: Model,
    rel: Relationship,
    attr: Attribute
): RelationshipAttributeLocation {
    return RelationshipAttributeLocation(
        createRelationshipLocation(model, rel),
        id = attr.id,
        key = attr.key,
        label = attr.name?.name ?: attr.key.value
    )
}