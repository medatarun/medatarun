package io.medatarun.model.actions

import io.medatarun.actions.actions.ActionUILocation
import io.medatarun.actions.ports.needs.*
import io.medatarun.model.domain.*
import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.model.domain.search.SearchFields
import io.medatarun.model.domain.search.SearchFilters
import io.medatarun.security.SecurityRuleNames
import io.medatarun.tags.core.domain.TagRef

sealed interface ModelAction {

    // ------------------------------------------------------------------------
    // Import
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "import",
        title = "Import model",
        description = "Imports a model from various locations.",
        uiLocations = [ActionUILocation.models],
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(mode = ActionDocSemanticsMode.NONE)
    )
    data class Import(
        @ActionParamDoc(
            name = "Source to import from",
            description = """
                        Source to import from.
                        
                        - Use an URL `https://...` to import from a remote location.
                        - Use `datasource:<datasource_name>` to import from a database. Available datasources are listed in configuration tools.
                        """,
            order = 10
        )
        val from: String,
        @ActionParamDoc(
            name = "Model name after import",
            description = """Name of the model once imported. If not specified, the name is generated automatically.""",
            order = 20
        )
        val modelName: String?,
        @ActionParamDoc(
            name = "Model key after import",
            description = """Stable business code of the model once imported. If not specified, the key is generated automatically.""",
            order = 30
        )
        val modelKey: ModelKey?,
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Inspect
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "inspect_models_json",
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model.",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(mode = ActionDocSemanticsMode.NONE)
    )
    class Inspect_Json : ModelAction

    @ActionDoc(
        key = "model_list",
        title = "Models list",
        description = "Returns a summary list of the models.",
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    class Model_List : ModelAction

    @ActionDoc(
        key = "model_export",
        title = "Export model",
        description = "Returns the exported view of a model.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Export(
        @ActionParamDoc(
            order = 10,
            name = "Model",
            description = "Model to export."
        )
        val modelRef: ModelRef,
    ) : ModelAction

    @ActionDoc(
        key = "model_export_version",
        title = "Export model at a specific version",
        description = "Returns the exported view of a model at a specific version.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )

    data class Model_Export_Version(
        @ActionParamDoc(
            order = 10,
            name = "Model",
            description = "Model to export."
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            order = 20,
            name = "Version",
            description = "Version of the model to export."
        )
        val version: ModelVersion,
    ) : ModelAction

    @ActionDoc(
        key = "model_compare",
        title = "Compare models",
        description = "Compares two model states and returns their differences.",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    data class Compare(
        @ActionParamDoc(
            order = 10,
            name = "Left model",
            description = "Left model to compare."
        )
        val leftModelRef: ModelRef,
        @ActionParamDoc(
            order = 20,
            name = "Left version",
            description = "Version of the left model to compare. If not specified, the current state is used."
        )
        val leftModelVersion: ModelVersion?,
        @ActionParamDoc(
            order = 30,
            name = "Right model",
            description = "Right model to compare."
        )
        val rightModelRef: ModelRef,
        @ActionParamDoc(
            order = 40,
            name = "Right version",
            description = "Version of the right model to compare. If not specified, the current state is used."
        )
        val rightModelVersion: ModelVersion?,
        @ActionParamDoc(
            order = 50,
            name = "Comparison scope",
            description = "Choose whether to compare only the structure of the two models, or the structure together with their names, descriptions, and other texts."
        )
        val scope: ModelDiffScope
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Model
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "model_create",
        title = "Create model",
        description = "Creates a new model with a key, a name, an optional description, and an optional version.",
        uiLocations = [ActionUILocation.models],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Create(
        @ActionParamDoc(
            order = 30,
            name = "Model key",
            description = "Provide a stable code for this model. This code is used to identify it uniquely across all models. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters."
        )
        val key: ModelKey,
        @ActionParamDoc(
            order = 20,
            name = "Name",
            description = "Name of this model."
        )
        val name: LocalizedText,
        @ActionParamDoc(
            order = 40,
            name = "Description",
            description = "Provide a comprehensive description of this model, including what it represents, its business meaning, its role in the company or in the application, its context, its rules, its usage, and any other useful information for someone discovering it."
        )
        val description: LocalizedMarkdown?,

        @ActionParamDoc(
            order = 50,
            name = "Version",
            description = "Initial version of this model, using semantic-version format. If not specified, the version will be `0.0.1`."
        )
        val version: ModelVersion?
    ) : ModelAction

    @ActionDoc(
        key = "model_copy",
        title = "Copy model",
        description = "Creates a copy of a model with a new key. The copied model keeps the same name and has its own lifecycle.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Copy(
        @ActionParamDoc(
            name = "Model",
            description = "Model to copy.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "New model key",
            description = "Provide a stable code for the copied model. This code is used to identify it uniquely across all models. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 20
        )
        val modelNewKey: ModelKey
    ) : ModelAction

    @ActionDoc(
        key = "model_update_key",
        title = "Update model key",
        description = "Updates the key of a model.",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateKey(
        @ActionParamDoc(
            name = "Model",
            description = "Model to update.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide the stable code used to identify this model. It must be unique across all models. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 20
        )
        val value: ModelKey
    ) : ModelAction

    @ActionDoc(
        key = "model_update_name",
        title = "Update model name",
        description = "Updates the name of a model.",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateName(
        @ActionParamDoc(
            name = "Model",
            description = "Model to update.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this model.",
            order = 20
        )
        val value: LocalizedText
    ) : ModelAction

    @ActionDoc(
        key = "model_update_description",
        title = "Update model description",
        description = "Updates the description of a model.",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateDescription(
        @ActionParamDoc(
            name = "Model",
            description = "Model to update.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of this model, including what it represents, its business meaning, its role in the company or in the application, its context, its rules, its usage, and any other useful information for someone discovering it.",
            order = 20
        )
        val value: LocalizedMarkdown?
    ) : ModelAction

    @ActionDoc(
        key = "model_update_authority",
        title = "Update model authority",
        description = "Updates whether this model serves as a canonical business reference or describes an existing system.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateAuthority(
        @ActionParamDoc(
            name = "Model",
            description = "Model to update.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Authority",
            description = "Choose whether this model serves as a canonical business reference or describes an existing system.",
            order = 20
        )
        val value: ModelAuthority
    ) : ModelAction


    @ActionDoc(
        key = "model_update_documentation_link",
        title = "Update model external documentation",
        description = "Updates the external documentation link of a model.",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_UpdateDocumentationHome(
        @ActionParamDoc(
            name = "Model",
            description = "Model to update.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "URL",
            description = "Link to the external documentation of this model.",
            order = 20
        )
        val value: String?
    ) : ModelAction


    @ActionDoc(
        key = "model_release",
        title = "Release version",
        description = "Releases a new version of a model. The new version must be greater than the previous one.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Release(
        @ActionParamDoc(
            name = "Model",
            description = "Model to release.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Version",
            description = "New version of this model.",
            order = 20
        )
        val value: ModelVersion
    ) : ModelAction

    @ActionDoc(
        key = "model_add_tag",
        title = "Add tag to model",
        description = "Adds a tag to a model.",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_AddTag(
        @ActionParamDoc(
            name = "Model",
            description = "Model to update.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to add to this model.",
            order = 20
        )
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "model_delete_tag",
        title = "Delete tag from model",
        description = "Removes a tag from a model.",
        uiLocations = [ActionUILocation.model_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_DeleteTag(
        @ActionParamDoc(
            name = "Model",
            description = "Model to update.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to remove from this model.",
            order = 20
        )
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "model_delete",
        title = "Delete model",
        description = "Removes a model and all of its entities from the runtime.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Model_Delete(
        @ActionParamDoc(
            name = "Model",
            description = "Model to delete.",
            order = 10
        )
        val modelRef: ModelRef
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "type_create",
        title = "Create type",
        description = "Creates a data type in an existing model.",
        uiLocations = [ActionUILocation.model_types],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_Create(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this data type will be created.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide a stable code for this data type. This code is used to identify it uniquely in the model. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 30
        )
        val typeKey: TypeKey,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this data type.",
            order = 20
        )
        val name: LocalizedText?,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of this data type, including how and where to use it, business rules, constraints or possible values.",
            order = 40
        )
        val description: LocalizedMarkdown?
    ) : ModelAction


    @ActionDoc(
        key = "type_update_key",
        title = "Update type key",
        description = "Updates the key of a data type.",
        uiLocations = [ActionUILocation.type_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateKey(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this data type is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Type",
            description = "Data type to update.",
            order = 20
        )
        val typeRef: TypeRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide the stable code used to identify this data type. It must be unique in the model. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 30
        )
        val value: TypeKey
    ) : ModelAction

    @ActionDoc(
        key = "type_update_name",
        title = "Update type name",
        description = "Updates the name of a data type.",
        uiLocations = [ActionUILocation.type_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateName(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this data type is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Type",
            description = "Data type to update.",
            order = 20
        )
        val typeRef: TypeRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this data type.",
            order = 30
        )
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "type_update_description",
        title = "Update type description",
        description = "Updates the description of a data type.",
        uiLocations = [ActionUILocation.type_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_UpdateDescription(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this data type is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Type",
            description = "Data type to update.",
            order = 20
        )
        val typeRef: TypeRef,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of this data type, including how and where to use it, business rules, constraints or possible values.",
            order = 30
        )
        val value: LocalizedMarkdown?
    ) :
        ModelAction

    @ActionDoc(
        key = "type_delete",
        title = "Delete type",
        description = "Deletes a data type from a model. This action fails if the data type is still used by entity attributes.",
        uiLocations = [ActionUILocation.type],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Type_Delete(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this data type is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Type",
            description = "Data type to delete.",
            order = 20
        )
        val typeRef: TypeRef
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "entity_create",
        title = "Create entity",
        description = "Creates an entity in an existing model.",
        uiLocations = [ActionUILocation.model_entities],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_Create(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this entity will be created.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide a stable code for this entity. This code is used to identify it uniquely in the model. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 30
        )
        val entityKey: EntityKey,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this entity.",
            order = 20
        )
        val name: LocalizedText?,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this entity represents in the domain. Explain the business concept behind it, what belongs in it, the main rules that apply to it, how it should be used, etc.",
            order = 40
        )
        val description: LocalizedMarkdown?,
        @ActionParamDoc(
            name = "Identity attribute key",
            description = "Provide a stable code for the attribute that will identify this entity. This first attribute is created together with the entity and is used to identify each occurrence of it.",
            order = 60
        )
        val identityAttributeKey: AttributeKey,
        @ActionParamDoc(
            name = "Identity attribute type",
            description = "Choose the data type of the attribute that will identify this entity. This first attribute is created together with the entity and is used to identify each occurrence of it.",
            order = 70
        )
        val identityAttributeType: TypeRef,
        @ActionParamDoc(
            name = "Identity attribute name",
            description = "Name of the attribute that will identify this entity. This first attribute is created together with the entity and is used to identify each occurrence of it.",
            order = 50
        )
        val identityAttributeName: LocalizedText?,
        @ActionParamDoc(
            name = "External documentation",
            description = "Link to external documentation for this entity.",
            order = 80
        )
        val documentationHome: String?
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_key",
        title = "Update entity key",
        description = "Updates the key of an entity.",
        uiLocations = [ActionUILocation.entity_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateKey(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide the stable code used to identify this entity. It must be unique in the model. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 30
        )
        val value: EntityKey
    ) : ModelAction


    @ActionDoc(
        key = "entity_update_name",
        title = "Update entity name",
        description = "Updates the name of an entity.",
        uiLocations = [ActionUILocation.entity_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateName(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this entity.",
            order = 30
        )
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_description",
        title = "Update entity description",
        description = "Updates the description of an entity.",
        uiLocations = [ActionUILocation.entity_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateDescription(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this entity represents in the domain. Explain the business concept behind it, what belongs in it, the main rules that apply to it, how it should be used, and any other information.",
            order = 30
        )
        val value: LocalizedMarkdown?
    ) : ModelAction

    @ActionDoc(
        key = "entity_update_documentation_link",
        title = "Update entity external documentation",
        description = "Updates the external documentation link of an entity.",
        uiLocations = [ActionUILocation.entity_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_UpdateDocumentationHome(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "URL",
            description = "Link to external documentation for this entity.",
            order = 30
        )
        val value: String?
    ) : ModelAction

    @ActionDoc(
        key = "entity_add_tag",
        title = "Add entity tag",
        description = "Adds a tag to an entity.",
        uiLocations = [ActionUILocation.entity_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_AddTag(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to add to this entity.",
            order = 30
        )
        val tag: TagRef
    ) : ModelAction


    @ActionDoc(
        key = "entity_delete_tag",
        title = "Delete entity tag",
        description = "Removes a tag from an entity.",
        uiLocations = [ActionUILocation.entity_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Entity_DeleteTag(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity to update.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to remove from this entity.",
            order = 30
        )
        val tag: TagRef
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
            description = "Model where this entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity to delete.",
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
        description = "Creates an attribute on an entity.",
        uiLocations = [ActionUILocation.entity_attributes],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_Create(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity that will contain attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute will be created.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this attribute.",
            order = 30
        )
        val name: LocalizedText?,
        @ActionParamDoc(
            name = "Attribute key",
            description = "Provide a stable code for this attribute. This code is used to identify it uniquely in the entity. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 40
        )
        val attributeKey: AttributeKey,
        @ActionParamDoc(
            name = "Data type",
            description = "Choose the data type of the information carried by this attribute.",
            order = 50
        )
        val type: TypeRef,
        @ActionParamDoc(
            name = "Optional",
            description = "Choose whether this attribute is required for all occurrences of the entity, or optional for some of them.",
            order = 60
        )
        val optional: Boolean = false,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
            order = 70
        )
        val description: LocalizedMarkdown?,
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_key",
        title = "Update entity attribute key",
        description = "Updates the key of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateKey(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to update.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide the stable code used to identify this attribute. It must be unique in the entity. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 40
        )
        val value: AttributeKey
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_update_name",
        title = "Update entity attribute name",
        description = "Updates the name of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateName(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to update.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this attribute.",
            order = 40
        )
        val value: LocalizedText?
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_description",
        title = "Update entity attribute description",
        description = "Updates the description of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateDescription(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to update.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
            order = 40
        )
        val value: LocalizedMarkdown?
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_type",
        title = "Update entity attribute type",
        description = "Updates the data type of an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateType(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to update.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
        @ActionParamDoc(
            name = "Data type",
            description = "Choose the data type of the information carried by this attribute.",
            order = 40
        )
        val value: TypeRef
    ) : ModelAction


    @ActionDoc(
        key = "entity_attribute_update_optional",
        title = "Update entity attribute optionality",
        description = "Updates whether an entity attribute is optional.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_UpdateOptional(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to update.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
        @ActionParamDoc(
            name = "Optional",
            description = "Choose whether this attribute is required for all occurrences of the entity, or optional for some of them.",
            order = 40
        )
        val value: Boolean
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_add_tag",
        title = "Add tag to entity attribute",
        description = "Adds a tag to an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_AddTag(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to update.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to add to this attribute.",
            order = 40
        )
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_delete_tag",
        title = "Delete tag from entity attribute",
        description = "Removes a tag from an entity attribute.",
        uiLocations = [ActionUILocation.entity_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_DeleteTag(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to update.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
        @ActionParamDoc(
            name = "Tag",
            description = "Tag to remove from this attribute.",
            order = 40
        )
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "entity_attribute_delete",
        title = "Delete entity attribute",
        description = "Deletes an attribute from an entity.",
        uiLocations = [ActionUILocation.entity_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityAttribute_Delete(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this attribute is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this attribute is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attribute",
            description = "Attribute to delete.",
            order = 30
        )
        val attributeRef: EntityAttributeRef,
    ) : ModelAction

    @ActionDoc(
        key = "entity_primary_key_update",
        title = "Update entity primary key",
        description = "Defines the primary key of an entity. Note that if the list of attributes is empty, the primary key will be removed.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class EntityPrimaryKey_Update(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity for which we set the primary key.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Attributes",
            description = "Attributes that participate in the primary key in order.",
            order = 30
        )
        val attributeRef: List<EntityAttributeRef>,
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "relationship_create",
        title = "Create relationship",
        description = "Creates a relationship between entities in a model.",
        uiLocations = [ActionUILocation.model_relationships, ActionUILocation.entity_relationships],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_Create(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this relationship will be created.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide a stable code for this relationship. This code is used to identify it uniquely in the model. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 30
        )
        val relationshipKey: RelationshipKey,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this relationship.",
            order = 20
        )
        val name: LocalizedText?,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this relationship represents in the domain. Explain what link or fact it expresses between entities, the main rules that apply to it, how to read it, and any useful examples or notes.",
            order = 40
        )
        val description: LocalizedMarkdown?,
        @ActionParamDoc(
            name = "Role A key",
            description = "Provide a stable code for the first role in this relationship. This code is used to identify the role within the relationship.",
            order = 60
        )
        val roleAKey: RelationshipRoleKey,
        @ActionParamDoc(
            name = "Role A entity",
            description = "Entity that participates in this relationship through the first role.",
            order = 70
        )
        val roleAEntityRef: EntityRef,
        @ActionParamDoc(
            name = "Role A name",
            description = "Name of the first role. Use it to express how this entity participates in the relationship.",
            order = 50
        )
        val roleAName: LocalizedText?,
        @ActionParamDoc(
            name = "Role A cardinality",
            description = "Choose how many occurrences of this entity may participate through the first role in one occurrence of the relationship.",
            order = 80
        )
        val roleACardinality: RelationshipCardinality,
        @ActionParamDoc(
            name = "Role B key",
            description = "Provide a stable code for the second role in this relationship. This code is used to identify the role within the relationship.",
            order = 100
        )
        val roleBKey: RelationshipRoleKey,
        @ActionParamDoc(
            name = "Role B entity",
            description = "Entity that participates in this relationship through the second role.",
            order = 110
        )
        val roleBEntityRef: EntityRef,
        @ActionParamDoc(
            name = "Role B name",
            description = "Name of the second role. Use it to express how this entity participates in the relationship.",
            order = 90
        )
        val roleBName: LocalizedText?,
        @ActionParamDoc(
            name = "Role B cardinality",
            description = "Choose how many occurrences of this entity may participate through the second role in one occurrence of the relationship.",
            order = 120
        )
        val roleBCardinality: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_update_key",
        title = "Update relationship key",
        description = "Updates the key of a relationship.",
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateKey(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this relationship is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship to update.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide the stable code used to identify this relationship. It must be unique in the model. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 30
        )
        val value: RelationshipKey,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_update_name",
        title = "Update relationship name",
        description = "Updates the name of a relationship.",
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateName(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this relationship is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship to update.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this relationship.",
            order = 30
        )
        val value: LocalizedText?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_update_description",
        title = "Update relationship description",
        description = "Updates the description of a relationship.",
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_UpdateDescription(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this relationship is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship to update.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this relationship represents in the domain. Explain what link or fact it expresses between entities, the main rules that apply to it, how to read it, and any useful examples or notes.",
            order = 30
        )
        val value: LocalizedMarkdown?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_create",
        title = "Create relationship role",
        description = "Creates a role in a relationship.",
        uiLocations = [ActionUILocation.relationship_roles],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_Create(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this relationship is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this role will be created.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(
            name = "Role key",
            description = "Provide a stable code for this role. This code is used to identify the role within the relationship.",
            order = 40
        )
        val roleKey: RelationshipRoleKey,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity that participates in the relationship through this role.",
            order = 50
        )
        val roleEntityRef: EntityRef,
        @ActionParamDoc(
            name = "Role name",
            description = "Name of this role. Use it to express how the entity participates in the relationship.",
            order = 30
        )
        val roleName: LocalizedText?,
        @ActionParamDoc(
            name = "Cardinality",
            description = "Choose how many occurrences of this entity may participate through this role in one occurrence of the relationship.",
            order = 60
        )
        val roleCardinality: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_key",
        title = "Update relationship role key",
        description = "Updates the key of a relationship role.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateKey(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship where this role is located.", order = 20)
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Role", description = "Role to update.", order = 30)
        val relationshipRoleRef: RelationshipRoleRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide the stable code used to identify this role within the relationship.",
            order = 40
        )
        val value: RelationshipRoleKey,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_entity",
        title = "Update relationship role entity",
        description = "Updates which entity participates through a relationship role.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateEntity(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship where this role is located.", order = 20)
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Role", description = "Role to update.", order = 30)
        val relationshipRoleRef: RelationshipRoleRef,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity that participates in the relationship through this role.",
            order = 40
        )
        val value: EntityRef,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_name",
        title = "Update relationship role name",
        description = "Updates the name of a relationship role.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateName(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship where this role is located.", order = 20)
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Role", description = "Role to update.", order = 30)
        val relationshipRoleRef: RelationshipRoleRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this role. Use it to express how the entity participates in the relationship.",
            order = 40
        )
        val value: LocalizedText?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_update_cardinality",
        title = "Update relationship role cardinality",
        description = "Updates the cardinality of a relationship role.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_UpdateCardinality(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship where this role is located.", order = 20)
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Role", description = "Role to update.", order = 30)
        val relationshipRoleRef: RelationshipRoleRef,
        @ActionParamDoc(
            name = "Cardinality",
            description = "Choose how many occurrences of this entity may participate through this role in one occurrence of the relationship.",
            order = 40
        )
        val value: RelationshipCardinality,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_role_delete",
        title = "Delete relationship role",
        description = "Deletes a role from a relationship. This action fails if fewer than two roles would remain.",
        uiLocations = [ActionUILocation.relationship_role],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipRole_Delete(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship where this role is located.", order = 20)
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Role", description = "Role to delete.", order = 30)
        val relationshipRoleRef: RelationshipRoleRef,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_add_tag",
        title = "Add tag to relationship",
        description = "Adds a tag to a relationship.",
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_AddTag(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship to update.", order = 20)
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Tag", description = "Tag to add to this relationship.", order = 30)
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_delete_tag",
        title = "Delete relationship tag",
        description = "Removes a tag from a relationship.",
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_DeleteTag(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship to update.", order = 20)
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Tag", description = "Tag to remove from this relationship.", order = 30)
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_delete",
        title = "Delete relationship",
        description = "Deletes a relationship.",
        uiLocations = [ActionUILocation.relationship],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_Delete(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(name = "Relationship", description = "Relationship to delete.", order = 20)
        val relationshipRef: RelationshipRef,
    ) : ModelAction


    @ActionDoc(
        key = "relationship_attribute_create",
        title = "Create relationship attribute",
        description = "Creates an attribute on a relationship.",
        uiLocations = [ActionUILocation.relationship_attributes],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Create(
        @ActionParamDoc(
            name = "Model",
            description = "Model where this relationship is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute will be created.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of this attribute.",
            order = 30
        )
        val name: LocalizedText?,
        @ActionParamDoc(
            name = "Attribute key",
            description = "Provide a stable code for this attribute. This code is used to identify it uniquely in the relationship. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 40
        )
        val attributeKey: AttributeKey,
        @ActionParamDoc(
            name = "Data type",
            description = "Choose the data type of the information carried by this attribute.",
            order = 50
        )
        val type: TypeRef,
        @ActionParamDoc(
            name = "Optional",
            description = "Choose whether this attribute is required for all occurrences of the relationship, or optional for some of them.",
            order = 60
        )
        val optional: Boolean,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
            order = 70
        )
        val description: LocalizedMarkdown?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_key",
        title = "Update relationship attribute key",
        description = "Updates the key of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateKey(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to update.", order = 30)
        val attributeRef: RelationshipAttributeRef,
        @ActionParamDoc(
            name = "Key",
            description = "Provide the stable code used to identify this attribute. It must be unique in the relationship. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
            order = 40
        )
        val value: AttributeKey
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_name",
        title = "Update relationship attribute name",
        description = "Updates the name of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateName(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to update.", order = 30)
        val attributeRef: RelationshipAttributeRef,
        @ActionParamDoc(name = "Name", description = "Name of this attribute.", order = 40)
        val value: LocalizedText?
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_description",
        title = "Update relationship attribute description",
        description = "Updates the description of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateDescription(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to update.", order = 30)
        val attributeRef: RelationshipAttributeRef,
        @ActionParamDoc(
            name = "Description",
            description = "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
            order = 40
        )
        val value: LocalizedMarkdown?
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_type",
        title = "Update relationship attribute type",
        description = "Updates the data type of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateType(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to update.", order = 30)
        val attributeRef: RelationshipAttributeRef,
        @ActionParamDoc(
            name = "Data type",
            description = "Choose the data type of the information carried by this attribute.",
            order = 40
        )
        val value: TypeRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_optional",
        title = "Update relationship attribute optionality",
        description = "Updates whether a relationship attribute is optional.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_UpdateOptional(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to update.", order = 30)
        val attributeRef: RelationshipAttributeRef,
        @ActionParamDoc(
            name = "Optional",
            description = "Choose whether this attribute is required for all occurrences of the relationship, or optional for some of them.",
            order = 40
        )
        val value: Boolean
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_add_tag",
        title = "Add tag to relationship attribute",
        description = "Adds a tag to a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_AddTag(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to update.", order = 30)
        val attributeRef: RelationshipAttributeRef,
        @ActionParamDoc(name = "Tag", description = "Tag to add to this attribute.", order = 40)
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_delete_tag",
        title = "Delete tag from relationship attribute",
        description = "Removes a tag from a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_DeleteTag(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to update.", order = 30)
        val attributeRef: RelationshipAttributeRef,
        @ActionParamDoc(name = "Tag", description = "Tag to remove from this attribute.", order = 40)
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_delete",
        title = "Delete relationship attribute",
        description = "Deletes an attribute from a relationship.",
        uiLocations = [ActionUILocation.relationship_attribute],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_Delete(
        @ActionParamDoc(name = "Model", description = "Model where this relationship is located.", order = 10)
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Relationship where this attribute is located.",
            order = 20
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(name = "Attribute", description = "Attribute to delete.", order = 30)
        val attributeRef: RelationshipAttributeRef,
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Business keys
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "business_key_create",
        title = "Create a business key",
        description = "Creates a business key to represent wich attributes of an entity uniquely identifies the objet in a business manner.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class BusinessKey_Create(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the entity containing this business key is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Name",
            description = "Business key name",
            order = 11
        )
        val name: LocalizedText?,
        @ActionParamDoc(
            name = "Key",
            description = "Business key's own key",
            order = 12
        )
        val key: BusinessKeyKey,
        @ActionParamDoc(
            name = "Description",
            description = "Business key description",
            order = 14
        )
        val description: LocalizedMarkdown?,
        @ActionParamDoc(
            name = "Entity",
            description = "Entity where this business key is located.",
            order = 20
        )
        val entityRef: EntityRef,
        @ActionParamDoc(
            name = "Participants",
            description = "List of attributes that participate in the business key, in order.",
            order = 30
        )
        val participants: List<EntityAttributeRef>,
    ) : ModelAction
    @ActionDoc(
        key = "business_key_update_key",
        title = "Update business key's key",
        description = "Changes the key of a business key.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class BusinessKey_Update_Key(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the business key is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Business key",
            description = "Business key to update.",
            order = 20
        )
        val businessKeyRef: BusinessKeyRef,
        @ActionParamDoc(
            name = "Key",
            description = "New key",
            order = 30
        )
        val value: BusinessKeyKey,
    ) : ModelAction

    @ActionDoc(
        key = "business_key_update_name",
        title = "Update business key name",
        description = "Changes the name of a business key.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class BusinessKey_Update_Name(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the business key is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Business key",
            description = "Business key to update.",
            order = 20
        )
        val businessKeyRef: BusinessKeyRef,
        @ActionParamDoc(
            name = "Name",
            description = "New name",
            order = 30
        )
        val value: LocalizedText?,
    ) : ModelAction
    @ActionDoc(
        key = "business_key_update_description",
        title = "Update business key description",
        description = "Changes the description of a business key.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class BusinessKey_Update_Description(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the business key is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Business key",
            description = "Business key to update.",
            order = 20
        )
        val businessKeyRef: BusinessKeyRef,
        @ActionParamDoc(
            name = "Name",
            description = "New name",
            order = 30
        )
        val value: LocalizedMarkdown?,
    ) : ModelAction

    @ActionDoc(
        key = "business_key_update_participants",
        title = "Update business key participants",
        description = "Changes the participants of a business key, meaning all attributes that define the business key meaning.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class BusinessKey_Update_Participants(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the business key is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Business key",
            description = "Business key to update.",
            order = 20
        )
        val businessKeyRef: BusinessKeyRef,
        @ActionParamDoc(
            name = "Participants",
            description = "List of the entity attributes that define the business key, in order.",
            order = 30
        )
        val value: List<EntityAttributeRef>,
    ) : ModelAction

    @ActionDoc(
        key = "business_key_delete",
        title = "Update business key participants",
        description = "Changes the participants of a business key, meaning all attributes that define the business key meaning.",
        uiLocations = [ActionUILocation.entity],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class BusinessKey_Delete(
        @ActionParamDoc(
            name = "Model",
            description = "Model where the business key is located.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Business key",
            description = "Business key to delete.",
            order = 20
        )
        val businessKeyRef: BusinessKeyRef
    ) : ModelAction


    // ------------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "search",
        title = "Search",
        description = "Searches models and model objects.",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(
            ActionDocSemanticsMode.DECLARED,
            ActionDocSemanticsIntent.READ,
            [],
            ["model", "tag", "entity", "entity_attribute", "relationship", "relationship_attribute"]
        )
    )
    data class Search(
        @ActionParamDoc(
            name = "Filters",
            description = "Filters used to narrow the search result. You can combine text filters and tag filters with AND or OR.",
            order = 10
        )
        val filters: SearchFilters,
        @ActionParamDoc(
            name = "Fields",
            description = "Fields returned for each search result. Use this to choose which information is included in the result, for example the location.",
            order = 20
        )
        val fields: SearchFields,
    ) : ModelAction

    // ------------------------------------------------------------------------
    // History
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "history_versions",
        title = "Versions",
        description = "Lists the released versions of a model.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(
            ActionDocSemanticsMode.DECLARED,
            ActionDocSemanticsIntent.READ,
            [],
            ["model", "tag", "entity", "entity_attribute", "relationship", "relationship_attribute"]
        )
    )
    data class HistoryVersions(
        @ActionParamDoc(
            name = "Model",
            description = "Model whose released versions you want to list.",
            order = 10
        )
        val modelRef: ModelRef
    ) : ModelAction

    @ActionDoc(
        key = "history_version_changes",
        title = "Version changes",
        description = "Lists the changes included in a version. When no version is provided, lists the changes since the last released version.",
        uiLocations = [ActionUILocation.model_overview],
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(
            ActionDocSemanticsMode.DECLARED,
            ActionDocSemanticsIntent.READ,
            [],
            ["model", "tag", "entity", "entity_attribute", "relationship", "relationship_attribute"]
        )
    )
    data class HistoryVersionChanges(
        @ActionParamDoc(
            name = "Model",
            description = "Model whose changes you want to inspect.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Version",
            description = "Version whose changes you want to list. If not specified, the result shows the changes since the last released version. Use semantic-version format.",
            order = 20
        )
        val version: ModelVersion?
    ) : ModelAction

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "maintenance_rebuild_caches",
        title = "Maintenance rebuild caches",
        description = """
            Rebuilds model application caches from stored events.
            
            Use this only as an exceptional maintenance action when data appears out of date.
            If you need to run it, we recommend contacting us on the project GitHub because it
            usually means you identified a bug.
        """,
        uiLocations = [ActionUILocation.hidden],
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(mode = ActionDocSemanticsMode.NONE)
    )
    class MaintenanceRebuildCaches : ModelAction


}
