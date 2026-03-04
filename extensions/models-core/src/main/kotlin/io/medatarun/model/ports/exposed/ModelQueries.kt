package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchQuery
import io.medatarun.model.domain.search.SearchResults
import java.util.*

interface ModelQueries {

    /**
     * Find a model by its id or throw [io.medatarun.model.domain.ModelNotFoundByKeyException]
     */
    fun findModelByKey(modelKey: ModelKey): ModelAggregate
    fun findModelById(modelId: ModelId): ModelAggregate
    fun findModel(modelRef: ModelRef): ModelAggregate
    fun findModelOptional(modelRef: ModelRef): ModelAggregate?

    fun findEntity(modelRef: ModelRef, entityRef: EntityRef): Entity

    /**
     * Returns complete list of all known model ids in this application instance
     */
    fun findAllModelIds(): List<ModelId>
    fun findAllModelSummaries(locale: Locale): List<ModelSummary>
    fun findEntityAttribute(modelRef: ModelRef, entityRef: EntityRef, attributeRef: EntityAttributeRef): Attribute
    fun findEntityAttributeOptional(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeRef: EntityAttributeRef
    ): Attribute?

    fun findType(modelRef: ModelRef, typeRef: TypeRef): ModelType

    fun search(query: SearchQuery): SearchResults
}