package io.medatarun.resources

import io.medatarun.model.model.*
import io.medatarun.resources.actions.ModelInspectAction
import io.medatarun.resources.actions.ModelInspectJsonAction
import io.medatarun.runtime.AppRuntime
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class ModelResource(private val runtime: AppRuntime): ResourceContainer {

    private val inspectAction = ModelInspectAction(runtime)
    private val inspectJsonAction = ModelInspectJsonAction(runtime)

    // ------------------------------------------------------------------------
    // Model
    // ------------------------------------------------------------------------

    @ResourceCommandDoc(
        title = "Create model definition",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version."
    )
    @Suppress("unused")
    fun createModel(id: String, name: String, description: String? = null, version: ModelVersion? = null) {
        logger.info("Create model $id ($name)")
        runtime.modelCmds.createModel(
            id = ModelId(id),
            name = LocalizedTextNotLocalized(name),
            description = description?.let { LocalizedTextNotLocalized(it) },
            version = version ?: ModelVersion("1.0.0")
        )
    }

    @ResourceCommandDoc(
        title = "Update model name",
        description = "Changes model name"
    )
    @Suppress("unused")
    fun updateModelName(id: String, name: String) {
        logger.info("Update model $id name $name")
        runtime.modelCmds.updateModelName(
            modelId = ModelId(id),
            name = LocalizedTextNotLocalized(name),
        )
    }

    @ResourceCommandDoc(
        title = "Update model description",
        description = "Changes model description"
    )
    @Suppress("unused")
    fun updateModelDescription(id: String, description: String?) {
        logger.info("Update model $id description $description")
        runtime.modelCmds.updateModelDescription(
            modelId = ModelId(id),
            description = description?.let { LocalizedTextNotLocalized(it) },
        )
    }

    @ResourceCommandDoc(
        title = "Update model description",
        description = "Changes model description"
    )
    @Suppress("unused")
    fun updateModelVersion(id: String, version: String) {
        logger.info("Update model $id version $version")
        runtime.modelCmds.updateModelVersion(
            modelId = ModelId(id),
            version = ModelVersion(version),
        )
    }

    @ResourceCommandDoc(
        title = "Delete model definition",
        description = "Removes a model and all of its entities from the runtime."
    )
    @Suppress("unused")
    fun deleteModel(id: String) {
        logger.info("Delete model $id")
        runtime.modelCmds.deleteModel(ModelId(id))
    }

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    @ResourceCommandDoc(
        title = "Create type",
        description = "Create type definition in an existing model, optionally supplying user-facing name and description."
    )
    @Suppress("unused")
    fun createType(
        modelId: String,
        typeId: String,
        name: String?,
        description: String?
    ) {
        runtime.modelCmds.createType(
            modelId = ModelId(modelId),
            initializer = ModelTypeInitializer(
                id = ModelTypeId(typeId),
                name = name?.let { LocalizedTextNotLocalized(it) },
                description = description?.let { LocalizedTextNotLocalized(it) },
            )
        )
    }

    @ResourceCommandDoc(
        title = "Update type name",
        description = "Updates a type name"
    )
    @Suppress("unused")
    fun updateTypeName(
        modelId: String,
        typeId: String,
        name: String?
    ) {
        runtime.modelCmds.updateType(
            modelId = ModelId(modelId),
            typeId = ModelTypeId(typeId),
            cmd = ModelTypeUpdateCmd.Name(name?.let { LocalizedTextNotLocalized(it) })
        )
    }

    @ResourceCommandDoc(
        title = "Update type description",
        description = "Updates a type description"
    )
    @Suppress("unused")
    fun updateTypeDescription(
        modelId: String,
        typeId: String,
        description: String?
    ) {
        runtime.modelCmds.updateType(
            modelId = ModelId(modelId),
            typeId = ModelTypeId(typeId),
            cmd = ModelTypeUpdateCmd.Description(description?.let { LocalizedTextNotLocalized(it) })
        )
    }


    @ResourceCommandDoc(
        title = "Delete type",
        description = "Delete type definition from an existing model. This will fail if this type is used in entity definition's attributes."
    )
    @Suppress("unused")
    fun deleteType(
        modelId: String,
        typeId: String,
        name: String?,
        description: String?
    ) {
        runtime.modelCmds.deleteType(
            modelId = ModelId(modelId),
            typeId = ModelTypeId(typeId),
        )
    }


    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

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
        runtime.modelCmds.createEntityDef(
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
        runtime.modelCmds.updateEntityDef(
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
        runtime.modelCmds.updateEntityDef(
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
        runtime.modelCmds.updateEntityDef(
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
        runtime.modelCmds.deleteEntityDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId)
        )
    }

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

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
        runtime.modelCmds.createEntityDefAttributeDef(
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
        runtime.modelCmds.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            cmd = AttributeDefUpdateCmd.Id(AttributeDefId(value))
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
        runtime.modelCmds.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            cmd = AttributeDefUpdateCmd.Name(value?.let { LocalizedTextNotLocalized(it) })
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
        runtime.modelCmds.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            cmd = AttributeDefUpdateCmd.Description(value?.let { LocalizedTextNotLocalized(it) })
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
        runtime.modelCmds.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            cmd = AttributeDefUpdateCmd.Type(ModelTypeId(value))
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
        runtime.modelCmds.updateEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId),
            cmd = AttributeDefUpdateCmd.Optional(value)
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
        runtime.modelCmds.deleteEntityDefAttributeDef(
            modelId = ModelId(modelId),
            entityDefId = EntityDefId(entityId),
            attributeDefId = AttributeDefId(attributeId)
        )
    }

    // ------------------------------------------------------------------------
    // Inspections
    // ------------------------------------------------------------------------

    @ResourceCommandDoc(
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes in the runtime."
    )
    @Suppress("unused")
    fun inspect(): String {
        return inspectAction.process()
    }


    @ResourceCommandDoc(
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model."
    )
    @Suppress("unused")
    fun inspectJson(): String {
        return inspectJsonAction.process()
    }


    override fun findCommandClass()= ModelCmd::class
    override fun dispatch(cmd: Any): Any? {
        return runtime.modelCmds.dispatch(cmd as ModelCmd)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModelResource::class.java)
    }
}
