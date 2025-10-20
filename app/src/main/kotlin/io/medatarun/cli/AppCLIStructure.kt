package io.medatarun.cli

import io.medatarun.model.model.*
import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger


class ModelCLIResource(private val runtime: AppRuntime) {
    @Suppress("unused")
    fun create(id: String, name: String, description: String? = null, version: ModelVersion? = null) {
        logger.cli("Create model $id ($name)")
        runtime.modelCmd.create(
            id = ModelId(id),
            name = LocalizedTextNotLocalized(name),
            description = description?.let { LocalizedTextNotLocalized(it) },
            version = version ?: ModelVersion("1.0.0")
        )
    }

    @Suppress("unused")
    fun createEntity(
        modelId: String,
        entityId: String,
        name: String?=null,
        description: String? = null,
    ) {
        logger.cli("Create entity $entityId in model $modelId ($name)")
        runtime.modelCmd.createEntity(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            name = name?.let { LocalizedTextNotLocalized(it) },
            description = description?.let { LocalizedTextNotLocalized(it) },
        )
    }

    @Suppress("unused")
    fun createEntityAttribute(
        modelId: String,
        entityId: String,
        attributeId: String,
        type: String,
        optional: Boolean = false,
        name: String?=null,
        description: String? = null
    ) {
        logger.cli("Create attribute $modelId.$entityId.$attributeId")
        runtime.modelCmd.createEntityAttribute(
            modelId= ModelId(modelId),
            entityId=ModelEntityId(entityId),
            attributeId= ModelAttributeId(attributeId),
            type = ModelTypeId(type),
            optional = optional,
            name = name?.let { LocalizedTextNotLocalized(it) },
            description =description?.let { LocalizedTextNotLocalized(it) },
        )
    }
    @Suppress("unused")
    fun inspect() {
        val modelId = runtime.modelQueries.findAllIds()
        modelId.forEach { modelId ->
            val model = runtime.modelQueries.findById(modelId)
            logger.cli("🌐 ${model.id.value}")
            model.entities.forEach { entity ->
                logger.cli("  📦 ${entity.id.value}")
                entity.attributes.forEach { attribute ->
                    logger.cli("    ${attribute.id.value}: ${attribute.type.value}${if (attribute.optional) "?" else ""}")
                }
            }
        }
    }
    companion object {
        private val logger = getLogger(ModelCLIResource::class)
    }
}

class ModelConfigCLIResource(private val runtime: AppRuntime) {
    @Suppress("unused")
    fun inspect() {
        logger.cli(runtime.extensionRegistry.inspectHumanReadable())
    }

    companion object {
        private val logger = getLogger(ModelConfigCLIResource::class)
    }
}

class ModelDataCLIResource(runtime: AppRuntime) {
    @Suppress("unused")
    fun import(file: String) {
        logger.cli("Importing file $file")
    }

    companion object {
        private val logger = getLogger(ModelDataCLIResource::class)
    }
}

class AppCLIResources(private val runtime: AppRuntime) {
    @Suppress("unused")
    val model = ModelCLIResource(runtime)

    @Suppress("unused")
    val config = ModelConfigCLIResource(runtime)

    @Suppress("unused")
    val data = ModelDataCLIResource(runtime)
}
