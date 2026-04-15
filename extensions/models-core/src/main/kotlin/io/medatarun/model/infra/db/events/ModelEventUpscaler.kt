package io.medatarun.model.infra.db.events

import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.model.ports.needs.ModelStorageCmdOld
import io.medatarun.model.ports.needs.StoreModelAggregateEntityCurrent
import io.medatarun.model.ports.needs.StoreModelAggregatePrimaryKey

/**
 * Convert old events to new
 */
class ModelEventUpscaler {
    fun upscale(cmd: ModelStorageCmdOld): List<ModelStorageCmd> {
        return when (cmd) {
            is ModelStorageCmdOld.CreateEntity -> upscaleCreateEntity(cmd)
            is ModelStorageCmdOld.UpdateEntityIdentifierAttribute -> upscaleUpdateEntityIdentifierAttribute(cmd)
            is ModelStorageCmdOld.StoreModelAggregate -> upscaleStoreModelAggregate(cmd)
        }
    }

    private fun upscaleUpdateEntityIdentifierAttribute(cmd: ModelStorageCmdOld.UpdateEntityIdentifierAttribute): List<ModelStorageCmd> {
        val next = ModelStorageCmd.Entity_PrimaryKey_Set(
            modelId = cmd.modelId,
            entityId = cmd.entityId,
            attributeIds = listOf(cmd.identifierAttributeId)
        )
        return listOf(next)
    }

    private fun upscaleCreateEntity(cmd: ModelStorageCmdOld.CreateEntity): List<ModelStorageCmd> {
        val cmdCreateEntity = ModelStorageCmd.CreateEntity(
            modelId = cmd.modelId,
            entityId = cmd.entityId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            documentationHome = cmd.documentationHome,
            origin = cmd.origin
        )
        val cmdCreateAttr = ModelStorageCmd.CreateEntityAttribute(
            modelId = cmd.modelId,
            entityId = cmd.entityId,
            attributeId = cmd.identityAttributeId,
            key = cmd.identityAttributeKey,
            name = cmd.identityAttributeName,
            description = cmd.identityAttributeDescription,
            typeId = cmd.identityAttributeTypeId,
            optional = cmd.identityAttributeIdOptional
        )
        val cmdSetPK = ModelStorageCmd.Entity_PrimaryKey_Set(
            modelId = cmd.modelId,
            entityId = cmd.entityId,
            attributeIds = listOf(cmd.identityAttributeId)
        )
        return listOf(cmdCreateEntity, cmdCreateAttr, cmdSetPK)
    }

    private fun upscaleStoreModelAggregate(cmd: ModelStorageCmdOld.StoreModelAggregate): List<ModelStorageCmd> {
        val next = ModelStorageCmd.StoreModelAggregate(
            model = cmd.model,
            types = cmd.types,
            entities = cmd.entities.map { e ->
                StoreModelAggregateEntityCurrent(
                    id = e.id,
                    key = e.key,
                    name = e.name,
                    description = e.description,
                    origin = e.origin,
                    documentationHome = e.documentationHome
                )
            },
            entityAttributes = cmd.entityAttributes,
            relationships = cmd.relationships,
            relationshipAttributes = cmd.relationshipAttributes,
            entityPrimaryKeys = cmd.entities.map { e ->
                StoreModelAggregatePrimaryKey(
                    entityId = e.id,
                    participants = listOf(e.identifierAttributeId)
                )
            },
            businessKeys = emptyList()
        )
        return listOf(next)
    }
}