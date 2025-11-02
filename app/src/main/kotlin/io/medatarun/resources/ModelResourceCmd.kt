package io.medatarun.resources

import io.medatarun.model.model.*

sealed interface ModelResourceCmd {

    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    @ResourceCommandDoc(
        title = "Import model",
        description = "Import a new model. The from attribute shall be an URL or a filesystem path. Detection is made based on the content of the file to detect original format. Supported formats depends on installed plugins."
    )
    data class Import(val from: String) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes in the runtime."
    )
    class Inspect() : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model."
    )
    class InspectJson() : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Create model",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version."
    )
    data class CreateModel(
        val id: String,
        val name: String,
        val description: String? = null,
        val version: ModelVersion? = null
    ) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Update model name",
        description = "Changes model name"
    )
    data class UpdateModelName(val id: String, val name: String) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Update model description",
        description = "Changes model description"
    )
    data class UpdateModelDescription(val id: String, val description: String?) : ModelResourceCmd


    @ResourceCommandDoc(
        title = "Update model description",
        description = "Changes model description"
    )
    data class UpdateModelVersion(val id: String, val version: String) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Delete model definition",
        description = "Removes a model and all of its entities from the runtime."
    )
    data class DeleteModel(val id: String) : ModelResourceCmd

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    @ResourceCommandDoc(
        title = "Create type",
        description = "Create type definition in an existing model, optionally supplying user-facing name and description."
    )
    data class CreateType(val modelId: String, val typeId: String, val name: String?, val description: String?) :
        ModelResourceCmd


    @ResourceCommandDoc(
        title = "Update type name",
        description = "Updates a type name"
    )
    data class UpdateTypeName(val modelId: String, val typeId: String, val name: String?) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Update type description",
        description = "Updates a type description"
    )
    data class UpdateTypeDescription(val modelId: String, val typeId: String, val description: String?) :
        ModelResourceCmd

    @ResourceCommandDoc(
        title = "Delete type",
        description = "Delete type definition from an existing model. This will fail if this type is used in entity definition's attributes."
    )
    data class DeleteType(val modelId: String, val typeId: String, val name: String?, val description: String?) :
        ModelResourceCmd

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @ResourceCommandDoc(
        title = "Create model entity",
        description = "Adds an entity to an existing model, optionally supplying user-facing name and description."
    )
    data class CreateEntity(
        val modelId: String,
        val entityId: String,
        val name: String? = null,
        val description: String? = null,
        val identityAttributeId: String,
        val identityAttributeType: ModelTypeId,
        val identityAttributeName: String? = null,
    ) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Update entity id",
        description = "Changes identifier of an entity."
    )
    data class UpdateEntityId(
        val modelId: String,
        val entityId: String,
        val value: String
    ) : ModelResourceCmd


    @ResourceCommandDoc(
        title = "Update entity title",
        description = "Changes the display title of an entity."
    )
    data class UpdateEntityName(
        val modelId: String,
        val entityId: String,
        val value: String?
    ) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Update entity description",
        description = "Changes the description of an entity."
    )
    data class UpdateEntityDescription(
        val modelId: String,
        val entityId: String,
        val value: String?
    ) : ModelResourceCmd


    @ResourceCommandDoc(
        title = "Delete model entity",
        description = "Removes an entity and all its attributes from the given model."
    )
    data class DeleteEntity(
        val modelId: String,
        val entityId: String
    ) : ModelResourceCmd


    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    @ResourceCommandDoc(
        title = "Create entity attribute",
        description = "Declares an attribute on an entity with its type, optional flag, and optional metadata."
    )
    data class CreateEntityAttribute(
        val modelId: String,
        val entityId: String,
        val attributeId: String,
        val type: String,
        val optional: Boolean = false,
        val name: String? = null,
        val description: String? = null
    ) : ModelResourceCmd



    @ResourceCommandDoc(
        title = "Update entity attribute id",
        description = "Changes identifier of an entity attribute."
    )
    data class UpdateEntityAttributeId(
        val modelId: String,
        val entityId: String,
        val attributeId: String,
        val value: String
    ): ModelResourceCmd

    @ResourceCommandDoc(
        title = "Update entity attribute name",
        description = "Changes the display title of an entity attribute."
    )
    data class UpdateEntityAttributeName(
        val modelId: String,
        val entityId: String,
        val attributeId: String,
        val value: String?
    ): ModelResourceCmd


    @ResourceCommandDoc(
        title = "Update entity attribute description",
        description = "Changes the description of an entity attribute."
    )
    data class UpdateEntityAttributeDescription(
        val modelId: String,
        val entityId: String,
        val attributeId: String,
        val value: String?
    ): ModelResourceCmd


    @ResourceCommandDoc(
        title = "Update entity attribute type",
        description = "Changes the declared type of an entity attribute."
    )
    data class UpdateEntityAttributeType(
        val modelId: String,
        val entityId: String,
        val attributeId: String,
        val value: String
    ): ModelResourceCmd


    @ResourceCommandDoc(
        title = "Update entity attribute optionality",
        description = "Changes whether an entity attribute is optional."
    )
    data class UpdateEntityAttributeOptional(
        val modelId: String,
        val entityId: String,
        val attributeId: String,
        val value: Boolean
    ): ModelResourceCmd

    @ResourceCommandDoc(
        title = "Delete entity attribute",
        description = "Removes an attribute from an entity within a model."
    )
    data class DeleteEntityAttribute(
        val modelId: String,
        val entityId: String,
        val attributeId: String
    ): ModelResourceCmd



    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    data class CreateRelationshipDef(
        val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelResourceCmd

    data class UpdateRelationshipDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelResourceCmd

    data class DeleteRelationshipDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId
    ) : ModelResourceCmd


    data class CreateRelationshipAttributeDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attr: AttributeDef
    ) : ModelResourceCmd

    data class UpdateRelationshipAttributeDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelResourceCmd

    data class DeleteRelationshipAttributeDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId
    ) : ModelResourceCmd


}