package io.medatarun.resources

import io.medatarun.model.model.LocalizedTextNotLocalized
import io.medatarun.model.model.ModelAttributeId
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelTypeId
import io.medatarun.model.model.ModelVersion
import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger

class ModelResource(private val runtime: AppRuntime) {
    @ResourceCommandDoc(
        title = "Create model definition",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version."
    )
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

    @ResourceCommandDoc(
        title = "Create model entity",
        description = "Adds an entity to an existing model, optionally supplying user-facing name and description."
    )
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

    @ResourceCommandDoc(
        title = "Create entity attribute",
        description = "Declares an attribute on an entity with its type, optional flag, and optional metadata."
    )
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
            entityId= ModelEntityId(entityId),
            attributeId= ModelAttributeId(attributeId),
            type = ModelTypeId(type),
            optional = optional,
            name = name?.let { LocalizedTextNotLocalized(it) },
            description =description?.let { LocalizedTextNotLocalized(it) },
        )
    }
    @ResourceCommandDoc(
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes in the runtime."
    )
    @Suppress("unused")
    fun inspect(): String {
        val buf = StringBuilder()
        val modelId = runtime.modelQueries.findAllIds()
        modelId.forEach { modelId ->
            val model = runtime.modelQueries.findById(modelId)
            buf.appendLine("🌐 ${model.id.value}")
            model.entities.forEach { entity ->
                buf.appendLine("  📦 ${entity.id.value}")
                entity.attributes.forEach { attribute ->
                    buf.appendLine("    ${attribute.id.value}: ${attribute.type.value}${if (attribute.optional) "?" else ""}")
                }
            }
        }
        return buf.toString()
    }
    companion object {
        private val logger = getLogger(ModelResource::class)
    }
}
