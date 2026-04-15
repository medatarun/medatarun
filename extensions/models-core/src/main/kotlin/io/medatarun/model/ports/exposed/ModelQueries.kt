package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*
import io.medatarun.model.domain.diff.ModelDiff
import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.model.domain.search.SearchQuery
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.tags.core.domain.TagId
import java.util.*

interface ModelQueries {

    // -------------------------------------------------------------------------
    // Model (just the model, not the full graph)
    // -------------------------------------------------------------------------

    /**
     * Find a model by its id or throw exception
     */
    fun findModelRoot(modelRef: ModelRef): Model
    fun findModelRootOptional(modelRef: ModelRef): Model?
    fun existsModel(modelRef: ModelRef): Boolean


    // -------------------------------------------------------------------------
    // Model aggregate
    // -------------------------------------------------------------------------

    /**
     * Find a model by its id or throw [io.medatarun.model.domain.ModelNotFoundByKeyException]
     */
    fun findModelAggregateByKey(modelKey: ModelKey): ModelAggregate
    fun findModelAggregateById(modelId: ModelId): ModelAggregate
    fun findModelAggregate(modelRef: ModelRef): ModelAggregate

    fun findModelAggregateOptional(modelRef: ModelRef): ModelAggregate?
    fun findModelAggregateAtVersion(modelRef: ModelRef, modelVersion: ModelVersion): ModelAggregate

    // -------------------------------------------------------------------------
    // Model events
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Types
    // -------------------------------------------------------------------------

    /**
     * Find a type, throw error if the model or type don't exist
     */
    fun findType(modelRef: ModelRef, typeRef: TypeRef): ModelType
    /**
     * Find a type, return null if the model or type don't exist
     */
    fun findTypeOptional(modelRef: ModelRef, typeRef: TypeRef): ModelType?

    /**
     * Find types, throw error if the model doesn't exist
     */
    fun findTypes(modelRef: ModelRef): List<ModelType>

    // -------------------------------------------------------------------------
    // Entities
    // -------------------------------------------------------------------------

    fun findEntity(modelRef: ModelRef, entityRef: EntityRef): Entity
    fun findEntityOptional(modelRef: ModelRef, entityRef: EntityRef): Entity?

    // -------------------------------------------------------------------------
    // Entity attributes
    // -------------------------------------------------------------------------

    fun findEntityAttribute(modelRef: ModelRef, entityRef: EntityRef, attributeRef: EntityAttributeRef): Attribute
    fun findEntityAttributeOptional(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeRef: EntityAttributeRef
    ): Attribute?

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    fun findRelationship(modelRef: ModelRef, relationshipRef: RelationshipRef): Relationship
    fun findRelationshipOptional(modelRef: ModelRef, relationshipRef: RelationshipRef): Relationship?

    // -------------------------------------------------------------------------
    // Relationship attributes
    // -------------------------------------------------------------------------

    fun findRelationshipAttribute(modelRef: ModelRef, relationshipRef: RelationshipRef, attributeRef: RelationshipAttributeRef): Attribute
    fun findRelationshipAttributeOptional(modelRef: ModelRef, relationshipRef: RelationshipRef, attributeRef: RelationshipAttributeRef): Attribute?

    // -------------------------------------------------------------------------
    // Entity PK
    // -------------------------------------------------------------------------

    fun findEntityPrimaryKeyOptional(modelRef: ModelRef, entityRef: EntityRef): EntityPrimaryKey?

    // -------------------------------------------------------------------------
    // Business keys
    // -------------------------------------------------------------------------

    /**
     * Find a business key, throw error if the model or business key doesn't exist.
     */
    fun findBusinessKey(modelRef: ModelRef, businessKeyRef: BusinessKeyRef): BusinessKey

    /**
     * Find a business key, return null if the model or business key doesn't exist.
     */
    fun findBusinessKeyOptional(modelRef: ModelRef, businessKeyRef: BusinessKeyRef): BusinessKey?

    /**
     * Find all business keys for a model, throw error if the model doesn't exist.
     */
    fun findBusinessKeys(modelRef: ModelRef): List<BusinessKey>

    // -------------------------------------------------------------------------
    // Tags
    // -------------------------------------------------------------------------

    fun findModelTags(modelRef: ModelRef): List<TagId>

    // -------------------------------------------------------------------------
    // Summaries
    // -------------------------------------------------------------------------

    /**
     * Returns a complete list of all known model ids in this application
     * instance.
     */
    fun findAllModelIds(): List<ModelId>
    fun findAllModelSummaries(locale: Locale): List<ModelSummary>

    // -------------------------------------------------------------------------
    // Diff
    // -------------------------------------------------------------------------

    /**
     * Compares the current model state when version is null, otherwise compares the released snapshot
     * associated with the requested version.
     */
    fun diff(
        leftModelRef: ModelRef,
        leftModelVersion: ModelVersion?,
        rightModelRef: ModelRef,
        rightModelVersion: ModelVersion?,
        scope: ModelDiffScope
    ): ModelDiff

    fun search(query: SearchQuery): SearchResults



}
