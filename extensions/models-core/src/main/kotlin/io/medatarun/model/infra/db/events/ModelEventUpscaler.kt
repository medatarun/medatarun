package io.medatarun.model.infra.db.events

import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.model.ports.needs.ModelStorageCmdOld

/**
 * Convert old events to new
 */
class ModelEventUpscaler {
    fun upscale(cmd: ModelStorageCmdOld): List<ModelStorageCmd> {
        return when(cmd) {
            is ModelStorageCmdOld.CreateEntity -> upscaleCreateEntity(cmd)
            is ModelStorageCmdOld.UpdateEntityIdentifierAttribute -> upscaleUpdateEntityIdentifierAttribute(cmd)
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
}