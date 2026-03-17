package io.medatarun.model.ports.exposed

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.*
import io.medatarun.model.domain.diff.ModelDiff
import io.medatarun.model.domain.diff.ModelDiffScope
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
    fun findModelAtVersion(modelRef: ModelRef, modelVersion: ModelVersion): ModelAggregate

    /**
     * Returns the list of all known versions of this model. Versions are
     * returned as [ModelChangeEvent], specifically the [ModelChangeEvent]
     * of type "model_release" that was used to create the version.
     *
     * Versions are returned from newer to oldest
     */
    fun findModelVersions(modelRef: ModelRef): List<ModelChangeEvent>

    /**
     * Lists all changes in the model included in the specified version
     * [modelVersion].
     *
     * Returns events from oldest to newer
     */
    fun findModelChangeEventsInVersion(modelRef: ModelRef, modelVersion: ModelVersion): List<ModelChangeEvent>

    /**
     * Lists all changes in the model since the last release event.
     *
     * Returns events from older to newer
     */
    fun findModelChangeEventsSinceLastReleaseEvent(modelRef: ModelRef): List<ModelChangeEvent>

    fun findEntity(modelRef: ModelRef, entityRef: EntityRef): Entity

    /**
     * Returns a complete list of all known model ids in this application
     * instance.
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

    fun diff(leftModelRef: ModelRef, rightModelRef: ModelRef, scope: ModelDiffScope): ModelDiff

    fun search(query: SearchQuery): SearchResults

}
