package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*
import java.util.*

interface ModelQueries {

    /**
     * Find a model by its id or throw [io.medatarun.model.domain.ModelNotFoundByKeyException]
     */
    fun findModelByKey(modelKey: ModelKey): Model
    fun findModelById(modelId: ModelId): Model
    fun findModel(modelRef: ModelRef): Model
    fun findModelOptional(modelRef: ModelRef): Model?

    fun findEntity(modelRef: ModelRef, entityRef: EntityRef): Entity

    /**
     * Returns complete list of all known model ids in this application instance
     */
    fun findAllModelIds(): List<ModelId>
    fun findAllModelSummaries(locale: Locale): List<ModelSummary>
    fun findEntityAttribute(modelRef: ModelRef, entityRef: EntityRef, attributeRef: EntityAttributeRef): Attribute
    fun findEntityAttributeOptional(modelRef: ModelRef, entityRef: EntityRef, attributeRef: EntityAttributeRef): Attribute?
    fun findType(modelRef: ModelRef, typeRef: TypeRef): ModelType


    fun findTags(tag: List<Hashtag>): List<TagSearchResult>
}