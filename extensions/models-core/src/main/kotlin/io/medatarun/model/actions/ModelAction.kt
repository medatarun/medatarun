package io.medatarun.model.actions

import io.medatarun.actions.actions.ActionUILocation
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.model.domain.*
import io.medatarun.security.SecurityRuleNames

@Suppress("ClassName")
sealed interface ModelAction {

    // ------------------------------------------------------------------------
    // Import
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "import",
        title = "Import model",
        description = "Import a new model. Detection is made based on the content of the file to detect original format. See installed plugins for supported formats.",
        uiLocations = [ActionUILocation.models],
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

    // ------------------------------------------------------------------------
    // Inspect
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "inspect_models_text",
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes.",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.SIGNED_IN

    )
    class Inspect_Human : ModelAction

    @ActionDoc(
        key = "inspect_models_json",
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model.",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class Inspect_Json : ModelAction

    @ActionDoc(
        key="model_list",
        title = "Models list",
        description = "Returns a summary list of the models",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class Model_List: ModelAction

    @ActionDoc(
        key="model_export",
        title = "Model export",
        description = "Returns an exporter view of the model",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Export(
        @ActionParamDoc(
            order = 1,
            name = "Model key",
            description = "Key of the model to export"
        )
        val modelKey: ModelKey,
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Model
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "model_create",
        title = "Create model",
        description = "Initializes a new model with the provided identifier, display name, optional description, and version.",
        uiLocations = [ActionUILocation.models],
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
        val name: LocalizedText,

        @ActionParamDoc(
            order = 3,
            name = "Description",
            description = "Provide a comprehensive description of your model, what is its purpose and usage."
        )
        val description: LocalizedMarkdown? = null,

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
        uiLocations = [ActionUILocation.model_overview],
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
        key = "model_update_key",
        title = "Update model key",
        description = "Changes model key",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateKey(val modelKey: ModelKey, val value: LocalizedText) : ModelAction

    @ActionDoc(
        key = "model_update_name",
        title = "Update model name",
        description = "Changes model name",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateName(val modelKey: ModelKey, val value: LocalizedText) : ModelAction

    @ActionDoc(
        key = "model_update_description",
        title = "Update model description",
        description = "Changes model description",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateDescription(val modelKey: ModelKey, val value: LocalizedMarkdown?) : ModelAction


    @ActionDoc(
        key = "model_update_version",
        title = "Update model version",
        description = "Changes model version",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateVersion(val modelKey: ModelKey, val value: ModelVersion) : ModelAction

    @ActionDoc(
        key = "model_add_tag",
        title = "Add tag to model",
        description = "Adds a tag to a model",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_AddTag(val modelKey: ModelKey, val tag: Hashtag) : ModelAction

    @ActionDoc(
        key = "model_delete_tag",
        title = "Delete tag from model",
        description = "Deletes a tag from a model.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_DeleteTag(val modelKey: ModelKey, val tag: Hashtag) : ModelAction

    @ActionDoc(
        key = "model_delete",
        title = "Delete model definition",
        description = "Removes a model and all of its entities from the runtime.",
        uiLocations = [ActionUILocation.model_overview],
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
        uiLocations = [ActionUILocation.model_types],
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
        val name: LocalizedText?,
        @ActionParamDoc(
            "Description",
            "Description of the type, when and what for it should be used. Express constraints and rules for this type.",
            order = 50
        )
        val description: LocalizedMarkdown?
    ) :
        ModelAction


    @ActionDoc(
        key = "type_update_name",
        title = "Update type name",
        description = "Updates a type name",
        uiLocations = [ActionUILocation.type],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateName(val modelKey: ModelKey, val typeKey: TypeKey, val name: LocalizedText?) : ModelAction

    @ActionDoc(
        key = "type_update_description",
        title = "Update type description",
        description = "Updates a type description",
        uiLocations = [ActionUILocation.type],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateDescription(val modelKey: ModelKey, val typeKey: TypeKey, val description: LocalizedMarkdown?) :
        ModelAction

    @ActionDoc(
        key = "type_delete",
        title = "Delete type",
        description = "Delete type definition from an existing model. This will fail if this type is used in entity definition's attributes.",
        uiLocations = [ActionUILocation.type],
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
        uiLocations = [ActionUILocation.model_entities],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_Create(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val name: LocalizedText? = null,
        val description: LocalizedMarkdown? = null,
        val identityAttributeKey: AttributeKey,
        val identityAttributeType: TypeKey,
        val identityAttributeName: LocalizedText? = null,
        val documentationHome: String? = null
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_key",
        title = "Update entity key",
        description = "Changes entity key.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateId(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: EntityKey
    ) : ModelAction


    @ActionDoc(
        key = "entity_update_title",
        title = "Update entity title",
        description = "Changes the display title of an entity.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateName(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_description",
        title = "Update entity description",
        description = "Changes the description of an entity.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateDescription(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val value: LocalizedMarkdown?
    ) : ModelAction

    @ActionDoc(
        key = "entity_add_tag",
        title = "Add entity tag",
        description = "Add a tag to an entity.",
        uiLocations = [ActionUILocation.entity],
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
        uiLocations = [ActionUILocation.entity],
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
        uiLocations = [ActionUILocation.entity],
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
        uiLocations = [ActionUILocation.entity_attributes],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_Create(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val type: TypeKey,
        val optional: Boolean = false,
        val name: LocalizedText? = null,
        val description: LocalizedMarkdown? = null
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_id",
        title = "Update entity attribute id",
        description = "Changes identifier of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateId(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: AttributeKey
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_update_name",
        title = "Update entity attribute name",
        description = "Changes the display title of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateName(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: LocalizedText?
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_description",
        title = "Update entity attribute description",
        description = "Changes the description of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateDescription(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: LocalizedMarkdown?
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_type",
        title = "Update entity attribute type",
        description = "Changes the declared type of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateType(
        val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val value: TypeKey
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_optional",
        title = "Update entity attribute optionality",
        description = "Changes whether an entity attribute is optional.",
        uiLocations = [ActionUILocation.entity_attribute],
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
        uiLocations = [ActionUILocation.entity_attribute],
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
        uiLocations = [ActionUILocation.entity_attribute],
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
        uiLocations = [ActionUILocation.entity_attribute],
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
        uiLocations = [ActionUILocation.model_relationships, ActionUILocation.entity_relationships],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_Create(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val name: LocalizedText?,
        val description: LocalizedMarkdown?,
        val roleAKey: RelationshipRoleKey,
        val roleAEntityKey: EntityKey,
        val roleAName: LocalizedText?,
        val roleACardinality: RelationshipCardinality,
        val roleBKey: RelationshipRoleKey,
        val roleBEntityKey: EntityKey,
        val roleBName: LocalizedText?,
        val roleBCardinality: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key="relationship_update_key",
        title="Update relationship key",
        description = "Changes the key of the relationship",
        uiLocations = [ActionUILocation.relationship],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateKey(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val value: RelationshipKey,
    ) : ModelAction

    @ActionDoc(
        key="relationship_update_name",
        title="Update relationship name",
        description = "Changes the name of the relationship",
        uiLocations = [ActionUILocation.relationship],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateName(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val value: LocalizedText?,
    ) : ModelAction

    @ActionDoc(
        key="relationship_update_description",
        title="Update relationship description",
        description = "Changes the description of the relationship",
        uiLocations = [ActionUILocation.relationship],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateDescription(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val value: LocalizedMarkdown?,
    ) : ModelAction

    @ActionDoc(
        key="relationship_role_create",
        title="Create relationship role",
        description = "Creates a new relationship role in relationship",
        uiLocations = [ActionUILocation.relationship_roles],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_Create(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val relationshipRoleKey: RelationshipRoleKey,
        val roleKey: RelationshipRoleKey,
        val roleEntityKey: EntityKey,
        val roleName: LocalizedText?,
        val roleCardinality: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key="relationship_role_update_key",
        title="Update relationship role key",
        description = "Changes the key of the relationship role",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateKey(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val relationshipRoleKey: RelationshipRoleKey,
        val value: RelationshipRoleKey,
    ) : ModelAction

    @ActionDoc(
        key="relationship_role_update_entity",
        title="Update relationship role entity",
        description = "Changes the entity that the relationship role represents",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateEntity(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val relationshipRoleKey: RelationshipRoleKey,
        val value: EntityKey,
    ) : ModelAction

    @ActionDoc(
        key="relationship_role_update_name",
        title="Update relationship role name",
        description = "Changes the name of the relationship role.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateName(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val relationshipRoleKey: RelationshipRoleKey,
        val value: LocalizedText?,
    ) : ModelAction

    @ActionDoc(
        key="relationship_role_update_cardinality",
        title="Update relationship role cardinality",
        description = "Changes the cardinality of the role within the relationship.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateCardinality(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val relationshipRoleKey: RelationshipRoleKey,
        val value: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key="relationship_role_delete",
        title="Delete relationship role",
        description = "Deletes relationship role. There must be at least two roles in a relationship left, otherwise this will fail.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_Delete(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val relationshipRoleKey: RelationshipRoleKey,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_add_tag",
        title = "Add tag to relationship",
        description = "Add tag to relationship",
        uiLocations = [ActionUILocation.relationship],
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
        uiLocations = [ActionUILocation.relationship],
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
        uiLocations = [ActionUILocation.relationship],
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
        uiLocations = [ActionUILocation.relationship_attributes],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Create(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val type: TypeKey,
        val optional: Boolean,
        val name: LocalizedText?,
        val description: LocalizedMarkdown?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_key",
        title = "Update relationship attribute key",
        description = "Changes key of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateKey(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val value: AttributeKey
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_name",
        title = "Update relationship attribute name",
        description = "Changes the display title of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateName(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_description",
        title = "Update relationship attribute description",
        description = "Changes the description of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateDescription(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val value: LocalizedMarkdown?
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_type",
        title = "Update relationship attribute type",
        description = "Changes the declared type of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateType(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val value: TypeKey
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_optional",
        title = "Update relationship attribute optionality",
        description = "Changes whether a relationship attribute is optional.",
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateOptional(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val value: Boolean
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_add_tag",
        title = "Add tag to relationship attribute",
        description = "Add a new tag to relationship attribute",
        uiLocations = [ActionUILocation.relationship_attribute],
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
        uiLocations = [ActionUILocation.relationship_attribute],
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
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Delete(
        val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    ) : ModelAction


}