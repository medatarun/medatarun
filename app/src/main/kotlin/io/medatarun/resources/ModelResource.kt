package io.medatarun.resources

import io.medatarun.model.model.*
import io.medatarun.runtime.AppRuntime
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

class ModelResource(private val runtime: AppRuntime) {
    @ResourceCommandDoc(
        title = "Create model definition",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version."
    )
    @Suppress("unused")
    fun create(id: String, name: String, description: String? = null, version: ModelVersion? = null) {
        logger.info("Create model $id ($name)")
        runtime.modelCmd.createModel(
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
        runtime.modelCmd.deleteModel(ModelId(id))
    }

    @ResourceCommandDoc(
        title = "Create model entity",
        description = "Adds an entity to an existing model, optionally supplying user-facing name and description."
    )
    @Suppress("unused")
    fun createEntity(
        modelId: String,
        entityId: String,
        name: String? = null,
        description: String? = null,
        identityAttributeId: String,
        identityAttributeType: ModelTypeId,
        identityAttributeName: String? = null,
    ) {
        logger.info("Create entity $entityId in model $modelId ($name)")
        runtime.modelCmd.createEntityDef(
            modelId = ModelId(modelId),
            entityDefInitializer = EntityDefInitializer(
                entityDefId = EntityDefId(entityId),
                name = name?.let { LocalizedTextNotLocalized(it) },
                description = description?.let { LocalizedTextNotLocalized(it) },
                identityAttribute = AttributeDefIdentityInitializer(
                    attributeDefId = AttributeDefId(identityAttributeId),
                    type = identityAttributeType,
                    name = name?.let { LocalizedTextNotLocalized(it) },
                    description = description?.let { LocalizedTextNotLocalized(it) },
                )
            ),

            )
    }

    @ResourceCommandDoc(
        title = "Update entity id",
        description = "Changes identifier of an entity."
    )
    @Suppress("unused")
    fun updateEntityId(
        modelId: String,
        entityId: String,
        value: String
    ) {
        logger.info("Update entity name $modelId.$entityId -> $value")
        runtime.modelCmd.updateEntityDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            cmd = EntityDefUpdateCmd.Id(EntityDefId(value))
        )
    }

    @ResourceCommandDoc(
        title = "Update entity title",
        description = "Changes the display title of an entity."
    )
    @Suppress("unused")
    fun updateEntityName(
        modelId: String,
        entityId: String,
        value: String?
    ) {
        logger.info("Update entity title $modelId.$entityId")
        runtime.modelCmd.updateEntityDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            cmd = EntityDefUpdateCmd.Name(value?.let { LocalizedTextNotLocalized(it) })
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
        value: String?
    ) {
        logger.info("Update entity description $modelId.$entityId")
        runtime.modelCmd.updateEntityDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            cmd = EntityDefUpdateCmd.Description(value?.let { LocalizedTextNotLocalized(it) })
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
        runtime.modelCmd.deleteEntityDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId)
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
        name: String? = null,
        description: String? = null
    ) {
        logger.info("Create attribute $modelId.$entityId.$attributeId")
        runtime.modelCmd.createEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefInitializer = AttributeDefInitializer(
                attributeDefId = AttributeDefId(attributeId),
                type = ModelTypeId(type),
                optional = optional,
                name = name?.let { LocalizedTextNotLocalized(it) },
                description = description?.let { LocalizedTextNotLocalized(it) },
            )
        )
    }

    @ResourceCommandDoc(
        title = "Update entity attribute id",
        description = "Changes identifier of an entity attribute."
    )
    @Suppress("unused")
    fun updateEntityAttributeId(
        modelId: String,
        entityId: String,
        attributeId: String,
        value: String
    ) {
        logger.info("Update attribute id $modelId.$entityId.$attributeId -> $value")
        runtime.modelCmd.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            target = AttributeDefUpdateCmd.Id(AttributeDefId(value))
        )
    }

    @ResourceCommandDoc(
        title = "Update entity attribute name",
        description = "Changes the display title of an entity attribute."
    )
    @Suppress("unused")
    fun updateEntityAttributeName(
        modelId: String,
        entityId: String,
        attributeId: String,
        value: String?
    ) {
        logger.info("Update attribute title $modelId.$entityId.$attributeId")
        runtime.modelCmd.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            target = AttributeDefUpdateCmd.Name(value?.let { LocalizedTextNotLocalized(it) })
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
        value: String?
    ) {
        logger.info("Update attribute description $modelId.$entityId.$attributeId")
        runtime.modelCmd.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            target = AttributeDefUpdateCmd.Description(value?.let { LocalizedTextNotLocalized(it) })
        )
    }

    @ResourceCommandDoc(
        title = "Update entity attribute type",
        description = "Changes the declared type of an entity attribute."
    )
    @Suppress("unused")
    fun updateEntityAttributeType(
        modelId: String,
        entityId: String,
        attributeId: String,
        value: String
    ) {
        logger.info("Update attribute type $modelId.$entityId.$attributeId -> $value")
        runtime.modelCmd.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            target = AttributeDefUpdateCmd.Type(ModelTypeId(value))
        )
    }

    @ResourceCommandDoc(
        title = "Update entity attribute optionality",
        description = "Changes whether an entity attribute is optional."
    )
    @Suppress("unused")
    fun updateEntityAttributeOptional(
        modelId: String,
        entityId: String,
        attributeId: String,
        value: Boolean
    ) {
        logger.info("Update attribute optional $modelId.$entityId.$attributeId -> $value")
        runtime.modelCmd.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            target = AttributeDefUpdateCmd.Optional(value)
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
        runtime.modelCmd.deleteEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId)
        )
    }

    @ResourceCommandDoc(
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes in the runtime."
    )
    @Suppress("unused")
    fun inspect(): String {
        val buf = StringBuilder()
        val modelId = runtime.modelQueries.findAllModelIds()
        modelId.forEach { modelId ->
            val model = runtime.modelQueries.findModelById(modelId)
            buf.appendLine("🌐 ${model.id.value}")
            model.entityDefs.forEach { entity ->
                buf.appendLine("  📦 ${entity.id.value}")
                entity.attributes.forEach { attribute ->
                    val name = attribute.id.value
                    val type = attribute.type.value
                    val optional = if (attribute.optional) "?" else ""
                    val pk = if (entity.identifierAttributeDefId == attribute.id) "🔑" else ""
                    buf.appendLine("    $name: $type$optional $pk")
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
                runtime.modelQueries.findAllModelIds().forEach { modelId ->
                    val model = runtime.modelQueries.findModelById(modelId)
                    add(buildJsonObject {
                        put("id", model.id.value)
                        put("version", model.version.value)
                        put("name", localizedTextToJson(model.name))
                        put("description", localizedTextToJson(model.description))
                        put("entities", buildJsonArray {
                            model.entityDefs.forEach { entity ->
                                add(buildJsonObject {
                                    put("id", entity.id.value)
                                    put("name", localizedTextToJson(entity.name))
                                    put("description", localizedTextToJson(entity.description))
                                    put("identifierAttribute", entity.identifierAttributeDefId.value)
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
