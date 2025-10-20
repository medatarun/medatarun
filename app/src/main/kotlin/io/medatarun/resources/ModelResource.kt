package io.medatarun.resources

import io.medatarun.model.model.LocalizedMarkdownNotLocalized
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.LocalizedTextNotLocalized
import io.medatarun.model.model.ModelAttributeId
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelTypeId
import io.medatarun.model.model.ModelVersion
import io.medatarun.runtime.AppRuntime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

class ModelResource(private val runtime: AppRuntime) {
    @ResourceCommandDoc(
        title = "Create model definition",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version."
    )
    @Suppress("unused")
    fun create(id: String, name: String, description: String? = null, version: ModelVersion? = null) {
        logger.info("Create model $id ($name)")
        runtime.modelCmd.create(
            id = ModelId(id),
            name = LocalizedTextNotLocalized(name),
            description = description?.let { LocalizedTextNotLocalized(it) },
            version = version ?: ModelVersion("1.0.0")
        )
    }

    @ResourceCommandDoc(
        title = "Delete model definition",
        description = "Removes a model and all of its entities from the runtime."
    )
    @Suppress("unused")
    fun delete(id: String) {
        logger.info("Delete model $id")
        runtime.modelCmd.delete(ModelId(id))
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
        logger.info("Create entity $entityId in model $modelId ($name)")
        runtime.modelCmd.createEntity(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            name = name?.let { LocalizedTextNotLocalized(it) },
            description = description?.let { LocalizedTextNotLocalized(it) },
        )
    }

    @ResourceCommandDoc(
        title = "Update entity name",
        description = "Changes the technical identifier of an entity."
    )
    @Suppress("unused")
    fun updateEntityName(
        modelId: String,
        entityId: String,
        newName: String
    ) {
        logger.info("Update entity name $modelId.$entityId -> $newName")
        runtime.modelCmd.updateEntityName(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            newEntityId = ModelEntityId(newName)
        )
    }

    @ResourceCommandDoc(
        title = "Update entity title",
        description = "Changes the display title of an entity."
    )
    @Suppress("unused")
    fun updateEntityTitle(
        modelId: String,
        entityId: String,
        title: String
    ) {
        logger.info("Update entity title $modelId.$entityId")
        runtime.modelCmd.updateEntityTitle(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            title = LocalizedTextNotLocalized(title)
        )
    }

    @ResourceCommandDoc(
        title = "Update entity description",
        description = "Changes the description of an entity."
    )
    @Suppress("unused")
    fun updateEntityDescription(
        modelId: String,
        entityId: String,
        description: String?
    ) {
        logger.info("Update entity description $modelId.$entityId")
        runtime.modelCmd.updateEntityDescription(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            description = description?.let { LocalizedMarkdownNotLocalized(it) }
        )
    }

    @ResourceCommandDoc(
        title = "Delete model entity",
        description = "Removes an entity and all its attributes from the given model."
    )
    @Suppress("unused")
    fun deleteEntity(
        modelId: String,
        entityId: String
    ) {
        logger.info("Delete entity $entityId from model $modelId")
        runtime.modelCmd.deleteEntity(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId)
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
        logger.info("Create attribute $modelId.$entityId.$attributeId")
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
        title = "Update entity attribute name",
        description = "Changes the technical identifier of an entity attribute."
    )
    @Suppress("unused")
    fun updateEntityAttributeName(
        modelId: String,
        entityId: String,
        attributeId: String,
        newName: String
    ) {
        logger.info("Update attribute name $modelId.$entityId.$attributeId -> $newName")
        runtime.modelCmd.updateEntityAttributeName(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            attributeId = ModelAttributeId(attributeId),
            newAttributeId = ModelAttributeId(newName)
        )
    }

    @ResourceCommandDoc(
        title = "Update entity attribute title",
        description = "Changes the display title of an entity attribute."
    )
    @Suppress("unused")
    fun updateEntityAttributeTitle(
        modelId: String,
        entityId: String,
        attributeId: String,
        title: String
    ) {
        logger.info("Update attribute title $modelId.$entityId.$attributeId")
        runtime.modelCmd.updateEntityAttributeTitle(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            attributeId = ModelAttributeId(attributeId),
            title = LocalizedTextNotLocalized(title)
        )
    }

    @ResourceCommandDoc(
        title = "Update entity attribute description",
        description = "Changes the description of an entity attribute."
    )
    @Suppress("unused")
    fun updateEntityAttributeDescription(
        modelId: String,
        entityId: String,
        attributeId: String,
        description: String?
    ) {
        logger.info("Update attribute description $modelId.$entityId.$attributeId")
        runtime.modelCmd.updateEntityAttributeDescription(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            attributeId = ModelAttributeId(attributeId),
            description = description?.let { LocalizedMarkdownNotLocalized(it) }
        )
    }

    @ResourceCommandDoc(
        title = "Delete entity attribute",
        description = "Removes an attribute from an entity within a model."
    )
    @Suppress("unused")
    fun deleteEntityAttribute(
        modelId: String,
        entityId: String,
        attributeId: String
    ) {
        logger.info("Delete attribute $modelId.$entityId.$attributeId")
        runtime.modelCmd.deleteEntityAttribute(
            modelId = ModelId(modelId),
            entityId = ModelEntityId(entityId),
            attributeId = ModelAttributeId(attributeId)
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

    @ResourceCommandDoc(
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model."
    )
    @Suppress("unused")
    fun inspectJson(): String {
        val root = buildJsonObject {
            put("models", buildJsonArray {
                runtime.modelQueries.findAllIds().forEach { modelId ->
                    val model = runtime.modelQueries.findById(modelId)
                    add(buildJsonObject {
                        put("id", model.id.value)
                        put("version", model.version.value)
                        put("name", localizedTextToJson(model.name))
                        put("description", localizedTextToJson(model.description))
                        put("entities", buildJsonArray {
                            model.entities.forEach { entity ->
                                add(buildJsonObject {
                                    put("id", entity.id.value)
                                    put("name", localizedTextToJson(entity.name))
                                    put("description", localizedTextToJson(entity.description))
                                    put("attributes", buildJsonArray {
                                        entity.attributes.forEach { attribute ->
                                            add(buildJsonObject {
                                                put("id", attribute.id.value)
                                                put("name", localizedTextToJson(attribute.name))
                                                put("description", localizedTextToJson(attribute.description))
                                                put("type", attribute.type.value)
                                                put("optional", attribute.optional)
                                            })
                                        }
                                    })
                                })
                            }
                        })
                    })
                }
            })
        }
        return root.toString()
    }

    private fun localizedTextToJson(value: LocalizedText?): JsonElement {
        return value?.let { text ->
            buildJsonObject {
                put("values", buildJsonObject {
                    text.all().forEach { (locale, content) ->
                        put(locale, content)
                    }
                })
            }
        } ?: JsonNull
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModelResource::class.java)
    }
}
