package io.medatarun.actions.providers.model

import io.medatarun.actions.runtime.ActionDoc
import io.medatarun.actions.runtime.ActionParamDoc
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.AttributeDefUpdateCmd
import io.medatarun.model.ports.exposed.RelationshipDefUpdateCmd

@Suppress("ClassName")
sealed interface ModelAction {

    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    @ActionDoc(
        title = "Import model",
        description = "Import a new model. The from attribute shall be an URL or a filesystem path. Detection is made based on the content of the file to detect original format. Supported formats depends on installed plugins.",
        uiLocation = "models"
    )
    data class Import(val from: String) : ModelAction

    @ActionDoc(
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes in the runtime.",
        uiLocation = "general"
    )
    class Inspect_Human() : ModelAction

    @ActionDoc(
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model.",
        uiLocation = "general"
    )
    class Inspect_Json() : ModelAction

    @ActionDoc(
        title = "Create model",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version.",
        uiLocation = "models"
    )
    data class Model_Create(
        @ActionParamDoc(
            order = 1,
            name = "Model key",
            description = "A key that uniquely names your model across all your models. Two models can not share the same key. Keys are limited to caracters and numbers, as well as some separators."
        )
        val modelKey: ModelKey,

        @ActionParamDoc(order = 2, name = "Name", description = "Human readable name of your model.")
        val name: String,

        @ActionParamDoc(
            order = 3,
            name = "Description",
            description = "Provide a comprehensive description of your model, what is its purpose and usage."
        )
        val description: String? = null,

        @ActionParamDoc(
            order = 4,
            name = "Version",
            description = "Initial version number, using semantic-version format."
        )
        val version: ModelVersion? = null
    ) : ModelAction

    @ActionDoc(
        title = "Update model name",
        description = "Changes model name",
        uiLocation = "model.name"
    )
    data class Model_UpdateName(val modelKey: ModelKey, val name: String) : ModelAction

    @ActionDoc(
        title = "Update model description",
        description = "Changes model description",
        uiLocation = "model.description"
    )
    data class Model_UpdateDescription(val modelKey: ModelKey, val description: String?) : ModelAction


    @ActionDoc(
        title = "Update model version",
        description = "Changes model version",
        uiLocation = "model.version"
    )
    data class Model_UpdateVersion(val modelKey: ModelKey, val version: String) : ModelAction

    @ActionDoc(
        title = "Add tag to model",
        description = "Adds a tag to a model",
        uiLocation = "model.tags"
    )
    data class Model_AddTag(val modelKey: ModelKey, val tag: Hashtag) : ModelAction

    @ActionDoc(
        title = "Delete tag from model",
        description = "Deletes a tag from a model.",
        uiLocation = "model.tags"
    )
    data class Model_DeleteTag(val modelKey: ModelKey, val tag: Hashtag) : ModelAction

    @ActionDoc(
        title = "Delete model definition",
        description = "Removes a model and all of its entities from the runtime.",
        uiLocation = "model"
    )
    data class Model_Delete(val modelKey: ModelKey) : ModelAction

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    @ActionDoc(
        title = "Create type",
        description = "Create type definition in an existing model, optionally supplying user-facing name and description.",
        uiLocation = "model"
    )
    data class Type_Create(
        val modelKey: ModelKey, val typeKey: TypeKey, val name: String?, val description: String?
    ) :
        ModelAction


    @ActionDoc(
        title = "Update type name",
        description = "Updates a type name",
        uiLocation = "type.name"
    )
    data class Type_UpdateName(val modelKey: ModelKey, val typeKey: TypeKey, val name: String?) : ModelAction

    @ActionDoc(
        title = "Update type description",
        description = "Updates a type description",
        uiLocation = "type.description"
    )
    data class Type_UpdateDescription(val modelKey: ModelKey, val typeKey: TypeKey, val description: String?) :
        ModelAction

    @ActionDoc(
        title = "Delete type",
        description = "Delete type definition from an existing model. This will fail if this type is used in entity definition's attributes.",
        uiLocation = "type"
    )
    data class Type_Delete(val modelKey: ModelKey, val typeKey: TypeKey) : ModelAction

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @ActionDoc(
        title = "Create model entity",
        description = "Adds an entity to an existing model, optionally supplying user-facing name and description.",
        uiLocation = "model",
    )
    data class Entity_Create(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val name: String? = null,
        val description: String? = null,
        val identityAttributeKey: AttributeKey,
        val identityAttributeType: TypeKey,
        val identityAttributeName: String? = null,
        val documentationHome: String? = null
    ) : ModelAction

    @ActionDoc(
        title = "Update entity id",
        description = "Changes identifier of an entity.",
        uiLocation = "entity.id"
    )
    data class Entity_UpdateId(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: String
    ) : ModelAction


    @ActionDoc(
        title = "Update entity title",
        description = "Changes the display title of an entity.",
        uiLocation = "entity.title"
    )
    data class Entity_UpdateName(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: String?
    ) : ModelAction

    @ActionDoc(
        title = "Update entity description",
        description = "Changes the description of an entity.",
        uiLocation = "entity.description"
    )
    data class Entity_UpdateDescription(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: String?
    ) : ModelAction

    @ActionDoc(
        title = "Add entity tag",
        description = "Add a tag to an entity.",
        uiLocation = "entity.tags"
    )
    data class Entity_AddTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val tag: Hashtag
    ) : ModelAction


    @ActionDoc(
        title = "Delete entity tag",
        description = "Changes the description of an entity.",
        uiLocation = "entity.tags"
    )
    data class Entity_DeleteTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val tag: Hashtag
    ) : ModelAction


    @ActionDoc(
        title = "Delete model entity",
        description = "Removes an entity and all its attributes from the given model.",
        uiLocation = "entity"
    )
    data class Entity_Delete(
        val modelKey: ModelKey,
        val entityKey: EntityKey
    ) : ModelAction


    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    @ActionDoc(
        title = "Create entity attribute",
        description = "Declares an attribute on an entity with its type, optional flag, and optional metadata.",
        uiLocation = "entity"
    )
    data class EntityAttribute_Create(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val type: TypeKey,
        val optional: Boolean = false,
        val name: String? = null,
        val description: String? = null
    ) : ModelAction


    @ActionDoc(
        title = "Update entity attribute id",
        description = "Changes identifier of an entity attribute.",
        uiLocation = "entityAttribute.id"
    )
    data class EntityAttribute_UpdateId(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String
    ) : ModelAction

    @ActionDoc(
        title = "Update entity attribute name",
        description = "Changes the display title of an entity attribute.",
        uiLocation = "entityAttribute.name"
    )
    data class EntityAttribute_UpdateName(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String?
    ) : ModelAction


    @ActionDoc(
        title = "Update entity attribute description",
        description = "Changes the description of an entity attribute.",
        uiLocation = "entityAttribute.description"
    )
    data class EntityAttribute_UpdateDescription(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String?
    ) : ModelAction


    @ActionDoc(
        title = "Update entity attribute type",
        description = "Changes the declared type of an entity attribute.",
        uiLocation = "entityAttribute.type"
    )
    data class EntityAttribute_UpdateType(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String
    ) : ModelAction


    @ActionDoc(
        title = "Update entity attribute optionality",
        description = "Changes whether an entity attribute is optional.",
        uiLocation = "entityAttribute.optional"
    )
    data class EntityAttribute_UpdateOptional(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: Boolean
    ) : ModelAction

    @ActionDoc(
        title = "Add tag to entity attribute",
        description = "Add tag to entity attribute",
        uiLocation = "entityAttribute.tags"
    )
    data class EntityAttribute_AddTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        title = "Delete tag from entity attribute",
        description = "Delete tag from entity attribute",
        uiLocation = "entityAttribute.tags"
    )
    data class EntityAttribute_DeleteTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        title = "Delete entity attribute",
        description = "Removes an attribute from an entity within a model.",
        uiLocation = "entityAttribute"
    )
    data class EntityAttribute_Delete(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey
    ) : ModelAction


    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    data class Relationship_Create(
        val modelKey: ModelKey,
        val initializer: RelationshipDef
    ) : ModelAction

    data class Relationship_Update(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelAction

    data class Relationship_AddTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val tag: Hashtag
    ) : ModelAction

    data class Relationship_DeleteTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val tag: Hashtag
    ) : ModelAction

    data class Relationship_Delete(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey
    ) : ModelAction


    data class RelationshipAttribute_Create(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attr: AttributeDef
    ) : ModelAction

    data class RelationshipAttribute_Update(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelAction

    data class RelationshipAttribute_AddTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    data class RelationshipAttribute_DeleteTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    data class RelationshipAttribute_Delete(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    ) : ModelAction


}