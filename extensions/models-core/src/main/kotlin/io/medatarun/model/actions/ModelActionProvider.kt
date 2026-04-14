package io.medatarun.model.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.ResourceLocator
import io.medatarun.security.AppActorResolver
import io.medatarun.tags.core.domain.TagQueries
import org.slf4j.LoggerFactory
import java.util.*

class ModelActionProvider(
    private val resourceLocator: ResourceLocator,
    private val extensionRegistry: ExtensionRegistry,
    private val modelCmds: ModelCmds,
    private val modelQueries: ModelQueries,
    private val actorResolver: AppActorResolver,
    private val tagQueries: TagQueries
) : ActionProvider<ModelAction> {

    override val actionGroupKey: String = ACTION_GROUP_KEY


    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user-facing actions.
     */
    override fun findCommandClass() = ModelAction::class

    override fun dispatch(action: ModelAction, actionCtx: ActionCtx): Any {

        val locale = Locale.getDefault()

        val handler = ModelActionHandler(modelCmds, modelQueries, tagQueries, resourceLocator, locale, actionCtx, extensionRegistry, actorResolver, )

        val result = when (action) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelAction.Import -> handler.modelImport(action)
            is ModelAction.Inspect_Json -> handler.modelInspectJson()
            is ModelAction.Compare -> handler.modelCompare(action)
            is ModelAction.Search -> handler.search(action)
            is ModelAction.MaintenanceRebuildCaches -> handler.maintenanceRebuildCaches()

            is ModelAction.Model_List -> handler.modelList(action)
            is ModelAction.Model_Export -> handler.modelExport(action)
            is ModelAction.Model_Export_Version -> handler.modelExportVersion(action)

            is ModelAction.Model_Create -> handler.modelCreate(action)
            is ModelAction.Model_Copy -> handler.modelCopy(action)
            is ModelAction.Model_UpdateKey -> handler.modelUpdateKey(action)
            is ModelAction.Model_UpdateName -> handler.modelUpdateName(action)
            is ModelAction.Model_UpdateDescription -> handler.modelUpdateDescription(action)
            is ModelAction.Model_UpdateAuthority -> handler.modelUpdateAuthority(action)
            is ModelAction.Model_UpdateDocumentationHome -> handler.modelUpdateDocumentationHome(action)
            is ModelAction.Model_Release -> handler.modelRelease(action)
            is ModelAction.Model_AddTag -> handler.modelAddTag(action)
            is ModelAction.Model_DeleteTag -> handler.modelDeleteTag(action)
            is ModelAction.Model_Delete -> handler.modelDelete(action)

            // ------------------------------------------------------------------------
            // Types
            // ------------------------------------------------------------------------

            is ModelAction.Type_Create -> handler.typeCreate(action)
            is ModelAction.Type_UpdateName -> handler.typeUpdateName(action)
            is ModelAction.Type_UpdateKey -> handler.typeUpdateKey(action)
            is ModelAction.Type_UpdateDescription -> handler.typeUpdateDescription(action)
            is ModelAction.Type_Delete -> handler.typeDelete(action)

            // ------------------------------------------------------------------------
            // Entities
            // ------------------------------------------------------------------------

            is ModelAction.Entity_Create -> handler.entityCreate(action)
            is ModelAction.Entity_UpdateKey -> handler.entityUpdateKey(action)
            is ModelAction.Entity_UpdateName -> handler.entityUpdateName(action)
            is ModelAction.Entity_UpdateDescription -> handler.entityUpdateDescription(action)
            is ModelAction.Entity_UpdateDocumentationHome -> handler.entityUpdateDocumentationHome(action)
            is ModelAction.Entity_AddTag -> handler.entityAddTag(action)
            is ModelAction.Entity_DeleteTag -> handler.entityDeleteTag(action)
            is ModelAction.Entity_Delete -> handler.entityDelete(action)

            // ------------------------------------------------------------------------
            // Entity attributes
            // ------------------------------------------------------------------------

            is ModelAction.EntityAttribute_Create -> handler.entityAttributeCreate(action)
            is ModelAction.EntityAttribute_UpdateKey -> handler.entityAttributeUpdateKey(action)
            is ModelAction.EntityAttribute_UpdateName -> handler.entityAttributeUpdateName(action)
            is ModelAction.EntityAttribute_UpdateDescription -> handler.entityAttributeUpdateDescription(action)
            is ModelAction.EntityAttribute_UpdateType -> handler.entityAttributeUpdateType(action)
            is ModelAction.EntityAttribute_UpdateOptional -> handler.entityAttributeUpdateOptional(action)
            is ModelAction.EntityAttribute_AddTag -> handler.entityAttributeAddTag(action)
            is ModelAction.EntityAttribute_DeleteTag -> handler.entityAttributeDeleteTag(action)
            is ModelAction.EntityAttribute_Delete -> handler.entityAttributeDelete(action)

            // ------------------------------------------------------------------------
            // Relationships
            // ------------------------------------------------------------------------

            is ModelAction.Relationship_Create -> handler.relationshipCreate(action)
            is ModelAction.Relationship_UpdateKey -> handler.relationshipUpdateKey(action)
            is ModelAction.Relationship_UpdateName -> handler.relationshipUpdateName(action)
            is ModelAction.Relationship_UpdateDescription -> handler.relationshipUpdateDescription(action)
            is ModelAction.Relationship_AddTag -> handler.relationshipAddTag(action)
            is ModelAction.Relationship_DeleteTag -> handler.relationshipDeleteTag(action)
            is ModelAction.Relationship_Delete -> handler.relationshipDelete(action)
            is ModelAction.RelationshipRole_Create -> handler.relationshipRoleCreate(action)
            is ModelAction.RelationshipRole_UpdateKey -> handler.relationshipRoleUpdateKey(action)
            is ModelAction.RelationshipRole_UpdateName -> handler.relationshipRoleUpdateName(action)
            is ModelAction.RelationshipRole_UpdateCardinality -> handler.relationshipRoleUpdateCardinality(action)
            is ModelAction.RelationshipRole_UpdateEntity -> handler.relationshipRoleUpdateEntity(action)
            is ModelAction.RelationshipRole_Delete -> handler.relationshipRoleDelete(action)
            is ModelAction.RelationshipAttribute_Create -> handler.relationshipAttributeCreate(action)
            is ModelAction.RelationshipAttribute_UpdateKey -> handler.relationshipAttributeUpdateKey(action)
            is ModelAction.RelationshipAttribute_UpdateType -> handler.relationshipAttributeUpdateType(action)
            is ModelAction.RelationshipAttribute_UpdateOptional -> handler.relationshipAttributeUpdateOptional(action)
            is ModelAction.RelationshipAttribute_UpdateName -> handler.relationshipAttributeUpdateName(action)
            is ModelAction.RelationshipAttribute_UpdateDescription -> handler.relationshipAttributeUpdateDescription(action)

            is ModelAction.RelationshipAttribute_AddTag -> handler.relationshipAttributeAddTag(action)
            is ModelAction.RelationshipAttribute_DeleteTag -> handler.relationshipAttributeDeleteTag(action)
            is ModelAction.RelationshipAttribute_Delete -> handler.relationshipAttributeDelete(action)

            // ------------------------------------------------------------------------
            // Primary keys
            // ------------------------------------------------------------------------

            is ModelAction.EntityPrimaryKey_Update -> handler.entityPrimaryKeyUpdate(action)
            
            // ------------------------------------------------------------------------
            // Business keys
            // ------------------------------------------------------------------------

            is ModelAction.BusinessKey_Create -> TODO()
            is ModelAction.BusinessKey_Update_Key -> TODO()
            is ModelAction.BusinessKey_Update_Name -> TODO()
            is ModelAction.BusinessKey_Update_Description -> TODO()
            is ModelAction.BusinessKey_Update_Participants -> TODO()
            is ModelAction.BusinessKey_Delete -> TODO()

            // History

            is ModelAction.HistoryVersions -> handler.historyVersions(action)
            is ModelAction.HistoryVersionChanges -> handler.historyChangesSinceVersion(action)
        }
        return result
    }


    companion object {

        /**
         * Changing this will break APIs or CLI. It is the name clients are using to reference action group
         */
        const val ACTION_GROUP_KEY = "model"
    }
}


