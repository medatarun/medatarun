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
            name = "URL to import from",
            description = """
                        Provide an URL `https://...` to import from a remote location. 
                        
                        Provide a filesystem path `/path/to/file` to import from a local file of your installation.
                        
                        Provide a `datasource:<datasource_name>` to import from a database. Available datasources are listed in configuration tools.     
                        """,
            order = 1
        )
        val from: String,
        @ActionParamDoc(
            name = "Model key after import",
            description = """Key of the model once imported. If not specified, the model key will be auto generated.""",
            order = 2
        )
        val modelKey: ModelKey?,
        @ActionParamDoc(
            name = "Model name after import",
            description = """Name of the model once imported. If not specified, the name will be auto generated.""",
            order = 3
        )
        val modelName: String?,
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
        key = "model_list",
        title = "Models list",
        description = "Returns a summary list of the models",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class Model_List : ModelAction

    @ActionDoc(
        key = "model_export",
        title = "Export model",
        description = "Returns an exporter view of the model",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Export(
        @ActionParamDoc(
            order = 1,
            name = "Model ref",
            description = "Reference of the model to export"
        )
        val modelRef: ModelRef,
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
            name = "Source model reference",
            description = "Reference of the model to be copied",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "New model key",
            description = "Key of the new model. Must be unique across all models.",
            order = 1
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
    data class Model_UpdateKey(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be updated",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "New model key",
            description = "New model key value. Must be unique across all models.",
            order = 1
        )
        val value: LocalizedText
    ) : ModelAction

    @ActionDoc(
        key = "model_update_name",
        title = "Update model name",
        description = "Changes model name",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateName(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be updated",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "New model name",
            description = "New model name.",
            order = 1
        )
        val value: LocalizedText
    ) : ModelAction

    @ActionDoc(
        key = "model_update_description",
        title = "Update model description",
        description = "Changes model description",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateDescription(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be updated",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "New description",
            description = "Fulltext rich description.",
            order = 1
        )
        val value: LocalizedMarkdown?
    ) : ModelAction


    @ActionDoc(
        key = "model_update_documentation_link",
        title = "Update model external documentation",
        description = "Provides a link to an external documentation.",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateDocumentationHome(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be updated",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "URL",
            description = "Link to the external documentation.",
            order = 1
        )
        val value: String?
    ) : ModelAction


    @ActionDoc(
        key = "model_update_version",
        title = "Update model version",
        description = "Changes model version",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateVersion(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be updated",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "New model version",
            description = "New model version.",
            order = 1
        )
        val value: ModelVersion
    ) : ModelAction

    @ActionDoc(
        key = "model_add_tag",
        title = "Add tag to model",
        description = "Adds a tag to a model",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_AddTag(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be updated",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Tag to add",
            description = "Tag to add",
            order = 1
        )
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "model_delete_tag",
        title = "Delete tag from model",
        description = "Deletes a tag from a model.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_DeleteTag(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be updated",
            order = 0
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Tag to delete",
            description = "Tag to delete",
            order = 1
        )
        val tag: Hashtag
    ) : ModelAction

    @ActionDoc(
        key = "model_delete",
        title = "Delete model definition",
        description = "Removes a model and all of its entities from the runtime.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Delete(
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to be delete",
            order = 0
        )
        val modelRef: ModelRef
    ) : ModelAction

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
            name = "Model reference",
            description = "Reference of the model to which the type will be added.",
            order = 10
        )
        val modelRef: ModelRef,
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
    ) : ModelAction


    @ActionDoc(
        key = "type_update_name",
        title = "Update type name",
        description = "Updates a type name",
        uiLocations = [ActionUILocation.type],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateName(
        val modelRef: ModelRef,
        val typeRef: TypeRef,
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "type_update_description",
        title = "Update type description",
        description = "Updates a type description",
        uiLocations = [ActionUILocation.type_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateDescription(
        val modelRef: ModelRef,
        val typeRef: TypeRef,
        val value: LocalizedMarkdown?
    ) :
        ModelAction

    @ActionDoc(
        key = "type_delete",
        title = "Delete type",
        description = "Delete type definition from an existing model. This will fail if this type is used in entity definition's attributes.",
        uiLocations = [ActionUILocation.type],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_Delete(
        val modelRef: ModelRef,
        val typeRef: TypeRef
    ) : ModelAction

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
        @ActionParamDoc(
            name = "Model reference",
            description = "Reference of the model to which the new entity will be added.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Key",
            description = "Unique key of entity in the model.",
            order = 20
        )
        val entityKey: EntityKey,
        @ActionParamDoc(
            name = "Name",
            description = "Display name of entity.",
            order = 30
        )
        val name: LocalizedText? = null,
        @ActionParamDoc(
            name = "Description",
            description = "Entity's description",
            order = 40
        )
        val description: LocalizedMarkdown? = null,
        @ActionParamDoc(
            name = "Identity attribute key",
            description = "Key of the attribute that will be created that acts as the entity's identifier.",
            order = 50
        )
        val identityAttributeKey: AttributeKey,
        @ActionParamDoc(
            name = "Identity attribute type",
            description = "Type of the attribute that will be created that acts as the entity's identifier.",
            order = 60
        )
        val identityAttributeType: TypeRef,
        @ActionParamDoc(
            name = "Identity attribute name",
            description = "Display name of the attribute that will be created that acts as the entity's identifier.",
            order = 70
        )
        val identityAttributeName: LocalizedText? = null,
        @ActionParamDoc(
            name = "External documentation",
            description = "Link to an external documentation.",
            order = 80
        )
        val documentationHome: String? = null
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_key",
        title = "Update entity key",
        description = "Changes entity key.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateKey(
        @ActionParamDoc(
            name = "Model",
            description = "Reference of the model where the entity to update is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Reference of the entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Key",
            description = "New key value. The key shall not be used for another entity",
            order = 30
        )
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
        @ActionParamDoc(
            name = "Model",
            description = "Reference of the model where the entity to update is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Reference of the entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Name",
            description = "New display name.",
            order = 30
        )
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_description",
        title = "Update entity description",
        description = "Changes the description of an entity.",
        uiLocations = [ActionUILocation.entity_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateDescription(
        @ActionParamDoc(
            name = "Model",
            description = "Reference of the model where the entity to update is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Reference of the entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Name",
            description = "New description.",
            order = 30
        )
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
        @ActionParamDoc(
            name = "Model",
            description = "Reference of the model where the entity to update is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Reference of the entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to add to entity.",
            order = 30
        )
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
        @ActionParamDoc(
            name = "Model",
            description = "Reference of the model where the entity to update is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Reference of the entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to delete.",
            order = 30
        )
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
        @ActionParamDoc(
            name = "Model",
            description = "Reference of the model where the entity to delete is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Reference of the entity to delete.",
            order = 20
        )
        val entityRef: EntityRef,
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
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeKey: AttributeKey,
        val type: TypeRef,
        val optional: Boolean = false,
        val name: LocalizedText? = null,
        val description: LocalizedMarkdown? = null
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_key",
        title = "Update entity attribute key",
        description = "Changes the key of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateId(
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
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
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val value: LocalizedText?
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_description",
        title = "Update entity attribute description",
        description = "Changes the description of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateDescription(
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
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
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val value: TypeRef
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_optional",
        title = "Update entity attribute optionality",
        description = "Changes whether an entity attribute is optional.",
        uiLocations = [ActionUILocation.entity_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateOptional(
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
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
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
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
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
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
        val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
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
        val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val name: LocalizedText?,
        val description: LocalizedMarkdown?,
        val roleAKey: RelationshipRoleKey,
        val roleAEntityRef: EntityRef,
        val roleAName: LocalizedText?,
        val roleACardinality: RelationshipCardinality,
        val roleBKey: RelationshipRoleKey,
        val roleBEntityRef: EntityRef,
        val roleBName: LocalizedText?,
        val roleBCardinality: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_update_key",
        title = "Update relationship key",
        description = "Changes the key of the relationship",
        uiLocations = [ActionUILocation.relationship],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateKey(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val value: RelationshipKey,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_update_name",
        title = "Update relationship name",
        description = "Changes the name of the relationship",
        uiLocations = [ActionUILocation.relationship],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateName(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val value: LocalizedText?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_update_description",
        title = "Update relationship description",
        description = "Changes the description of the relationship",
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateDescription(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val value: LocalizedMarkdown?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_create",
        title = "Create relationship role",
        description = "Creates a new relationship role in relationship",
        uiLocations = [ActionUILocation.relationship_roles],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_Create(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleKey: RelationshipRoleKey,
        val roleKey: RelationshipRoleKey,
        val roleEntityRef: EntityRef,
        val roleName: LocalizedText?,
        val roleCardinality: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_key",
        title = "Update relationship role key",
        description = "Changes the key of the relationship role",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateKey(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: RelationshipRoleKey,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_entity",
        title = "Update relationship role entity",
        description = "Changes the entity that the relationship role represents",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateEntity(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: EntityRef,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_name",
        title = "Update relationship role name",
        description = "Changes the name of the relationship role.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateName(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: LocalizedText?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_cardinality",
        title = "Update relationship role cardinality",
        description = "Changes the cardinality of the role within the relationship.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateCardinality(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_delete",
        title = "Delete relationship role",
        description = "Deletes relationship role. There must be at least two roles in a relationship left, otherwise this will fail.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_Delete(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_add_tag",
        title = "Add tag to relationship",
        description = "Add tag to relationship",
        uiLocations = [ActionUILocation.relationship],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_AddTag(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
    ) : ModelAction


    @ActionDoc(
        key = "relationship_attribute_create",
        title = "Create relationship attribute",
        description = "Creates a new relationship attribute",
        uiLocations = [ActionUILocation.relationship_attributes],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Create(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeKey: AttributeKey,
        val type: TypeRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_description",
        title = "Update relationship attribute description",
        description = "Changes the description of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateDescription(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val value: TypeRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_optional",
        title = "Update relationship attribute optionality",
        description = "Changes whether a relationship attribute is optional.",
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateOptional(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
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
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
    ) : ModelAction


}