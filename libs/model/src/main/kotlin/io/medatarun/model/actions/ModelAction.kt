package io.medatarun.model.actions

import io.medatarun.actions.actions.ActionUILocation
import io.medatarun.actions.actions.SecurityRuleNames
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.AttributeDefUpdateCmd
import io.medatarun.model.ports.exposed.RelationshipDefUpdateCmd

@Suppress("ClassName")
sealed interface ModelAction {

    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "import",
        title = "Import model",
        description = "Import a new model. Detection is made based on the content of the file to detect original format. See installed plugins for supported formats.",
        uiLocation = ActionUILocation.models,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Import(
        @ActionParamDoc(
            "URL to import from",
            """
            Provide an URL `https://...` to import from a remote location. 
            
            Provide a filesystem path `/path/to/file` to import from a local file of your installation.
            
            Provide a `datasource:<datasource_name>` to import from a database. Available datasources are listed in configuration tools.     
            """
        )
        val from: String
    ) : ModelAction

    @ActionDoc(
        key = "inspect_models_text",
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes.",
        uiLocation = ActionUILocation.general,
        securityRule = SecurityRuleNames.SIGNED_IN

    )
    class Inspect_Human() : ModelAction

    @ActionDoc(
        key = "inspect_models_json",
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model.",
        uiLocation = ActionUILocation.general,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class Inspect_Json() : ModelAction

    @ActionDoc(
        key = "model_create",
        title = "Create model",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version.",
        uiLocation = ActionUILocation.models,
        securityRule = SecurityRuleNames.SIGNED_IN
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
        key = "model_copy",
        title = "Copy model",
        description = "Make a copy of a model, giving him a new name. The copied model lifecycle will be independant",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Copy(
        @ActionParamDoc(
            name = "Source model key",
            description = "Key of the model to be copied",
            order = 0
        )
        val modelKey: ModelKey,
        @ActionParamDoc(
            name = "New model key",
            description = "Key of the new model. Must be unique across all models.",
            order = 0
        )
        val modelNewKey: ModelKey
    ) : ModelAction

    @ActionDoc(
        key = "model_update_name",
        title = "Update model name",
        description = "Changes model name",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateName(val modelKey: ModelKey, val name: String) : ModelAction

    @ActionDoc(
        key = "model_update_description",
        title = "Update model description",
        description = "Changes model description",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateDescription(val modelKey: ModelKey, val description: String?) : ModelAction


    @ActionDoc(
        key = "model_update_version",
        title = "Update model version",
        description = "Changes model version",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateVersion(val modelKey: ModelKey, val version: String) : ModelAction

    @ActionDoc(
        key = "model_add_tag",
        title = "Add tag to model",
        description = "Adds a tag to a model",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_AddTag(val modelKey: ModelKey, val tag: Hashtag) : ModelAction

    @ActionDoc(
        key = "model_delete_tag",
        title = "Delete tag from model",
        description = "Deletes a tag from a model.",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_DeleteTag(val modelKey: ModelKey, val tag: Hashtag) : ModelAction

    @ActionDoc(
        key = "model_delete",
        title = "Delete model definition",
        description = "Removes a model and all of its entities from the runtime.",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Delete(val modelKey: ModelKey) : ModelAction

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "type_create",
        title = "Create type",
        description = "Create type definition in an existing model, optionally supplying user-facing name and description.",
        uiLocation = ActionUILocation.model_types,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_Create(
        @ActionParamDoc(
            name = "Model key",
            description = "Key of the model to which the type will be added.",
            order = 10
        )
        val modelKey: ModelKey,
        @ActionParamDoc(
            name = "Key",
            description = "Key of the type to be created. Must be unique in the model.",
            order = 20
        )
        val typeKey: TypeKey,
        @ActionParamDoc(
            name = "Name",
            description = "Display name of the type.",
            order = 30
        )
        val name: String?,
        @ActionParamDoc(
            "Description",
            "Description of the type, when and what for it should be used. Express constraints and rules for this type.",
            order = 50
        )
        val description: String?
    ) :
        ModelAction


    @ActionDoc(
        key = "type_update_name",
        title = "Update type name",
        description = "Updates a type name",
        uiLocation = ActionUILocation.type,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateName(val modelKey: ModelKey, val typeKey: TypeKey, val name: String?) : ModelAction

    @ActionDoc(
        key = "type_update_description",
        title = "Update type description",
        description = "Updates a type description",
        uiLocation = ActionUILocation.type,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateDescription(val modelKey: ModelKey, val typeKey: TypeKey, val description: String?) :
        ModelAction

    @ActionDoc(
        key = "type_delete",
        title = "Delete type",
        description = "Delete type definition from an existing model. This will fail if this type is used in entity definition's attributes.",
        uiLocation = ActionUILocation.type,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_Delete(val modelKey: ModelKey, val typeKey: TypeKey) : ModelAction

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "entity_create",
        title = "Create model entity",
        description = "Adds an entity to an existing model, optionally supplying user-facing name and description.",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
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
        key = "entity_update_id",
        title = "Update entity id",
        description = "Changes identifier of an entity.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateId(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: String
    ) : ModelAction


    @ActionDoc(
        key = "entity_update_title",
        title = "Update entity title",
        description = "Changes the display title of an entity.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateName(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: String?
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_description",
        title = "Update entity description",
        description = "Changes the description of an entity.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateDescription(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: String?
    ) : ModelAction

    @ActionDoc(
        key = "entity_add_tag",
        title = "Add entity tag",
        description = "Add a tag to an entity.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_AddTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val tag: Hashtag
    ) : ModelAction


    @ActionDoc(
        key = "entity_delete_tag",
        title = "Delete entity tag",
        description = "Changes the description of an entity.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_DeleteTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val tag: Hashtag
    ) : ModelAction


    @ActionDoc(
        key = "entity_delete",
        title = "Delete model entity",
        description = "Removes an entity and all its attributes from the given model.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_Delete(
        val modelKey: ModelKey,
        val entityKey: EntityKey
    ) : ModelAction


    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "entity_attribute_create",
        title = "Create entity attribute",
        description = "Declares an attribute on an entity with its type, optional flag, and optional metadata.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
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
        key = "entity_attribute_update_id",
        title = "Update entity attribute id",
        description = "Changes identifier of an entity attribute.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateId(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_update_name",
        title = "Update entity attribute name",
        description = "Changes the display title of an entity attribute.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateName(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String?
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_description",
        title = "Update entity attribute description",
        description = "Changes the description of an entity attribute.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateDescription(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String?
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_type",
        title = "Update entity attribute type",
        description = "Changes the declared type of an entity attribute.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateType(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: String
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_optional",
        title = "Update entity attribute optionality",
        description = "Changes whether an entity attribute is optional.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateOptional(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: Boolean
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_add_tag",
        title = "Add tag to entity attribute",
        description = "Add tag to entity attribute",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_AddTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_delete_tag",
        title = "Delete tag from entity attribute",
        description = "Delete tag from entity attribute",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_DeleteTag(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_delete",
        title = "Delete entity attribute",
        description = "Removes an attribute from an entity within a model.",
        uiLocation = ActionUILocation.entity,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_Delete(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey
    ) : ModelAction


    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "relationship_create",
        title = "Create relationship",
        description = "Create a new relationship between entities within a model.",
        uiLocation = ActionUILocation.model,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_Create(
        val modelKey: ModelKey,
        val initializer: RelationshipDef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_update",
        title = "Update relationship",
        description = "Update relationship details (syntax will change soon)",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_Update(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelAction

    @ActionDoc(
        key = "relationship_add_tag",
        title = "Add tag to relationship",
        description = "Add tag to relationship",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_AddTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "relationship_delete_tag",
        title = "Delete relationship tag",
        description = "Delete tag from relationship",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_DeleteTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "relationship_delete",
        title = "Delete relationship",
        description = "Delete this relationship",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_Delete(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey
    ) : ModelAction


    @ActionDoc(
        key = "relationship_attribute_create",
        title = "Create relationship attribute",
        description = "Creates a new relationship attribute",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Create(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attr: AttributeDef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update",
        title = "Updates relationship attribute",
        description = "Updates a relationship attribute (subject to change)",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Update(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_add_tag",
        title = "Add tag to relationship attribute",
        description = "Add a new tag to relationship attribute",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_AddTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_delete_tag",
        title = "Delete tag from relationship attribute",
        description = "Delete tag from relationship attribute",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_DeleteTag(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_delete",
        title = "Delete relationship attribute",
        description = "Delete relationship attribute",
        uiLocation = ActionUILocation.relationship,
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Delete(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    ) : ModelAction


}