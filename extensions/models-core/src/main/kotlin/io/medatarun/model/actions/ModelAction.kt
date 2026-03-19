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
            description = "Choose whether this attribute is is optional for some occurrences of the entity.",
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
            description = "Choose whether this attribute may be left empty for some occurrences of the entity.",
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
        uiLocations = [ActionUILocation.relationship_hidden],
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
        uiLocations = [ActionUILocation.relationship_hidden],
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
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_AddTag(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_delete_tag",
        title = "Delete relationship tag",
        description = "Delete tag from relationship",
        uiLocations = [ActionUILocation.relationship_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class Relationship_DeleteTag(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val tag: TagRef
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
        @ActionParamDoc(
            name = "Model",
            description = "Reference of the model where the relationship is.",
            order = 10
        )
        val modelRef: ModelRef,
        @ActionParamDoc(
            name = "Relationship",
            description = "Reference of the relationship where to create attribute.",
            order = 11
        )
        val relationshipRef: RelationshipRef,
        @ActionParamDoc(
            name = "Name",
            description = "Name of the attribute, human-readable.",
            order = 20
        )
        val name: LocalizedText?,
        @ActionParamDoc(
            name = "Attribute key",
            description = "Unique key of the attribute in its relationship.",
            order = 30
        )
        val attributeKey: AttributeKey,
        @ActionParamDoc(
            name = "Data type",
            description = "Data type of this attribute, choosed from the types of the model.",
            order = 40
        )
        val type: TypeRef,
        @ActionParamDoc(
            name = "Attribute is optional",
            description = "Indicates this attribute is not required.",
            order = 45
        )
        val optional: Boolean,
        @ActionParamDoc(
            name = "Description",
            description = "Attribute's full description.",
            order = 50
        )
        val description: LocalizedMarkdown?,
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_update_key",
        title = "Update relationship attribute key",
        description = "Changes key of a relationship attribute.",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
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
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
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
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
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
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
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
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_AddTag(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val tag: TagRef
    ) : ModelAction

    @ActionDoc(
        key = "relationship_attribute_delete_tag",
        title = "Delete tag from relationship attribute",
        description = "Delete tag from relationship attribute",
        uiLocations = [ActionUILocation.relationship_attribute_hidden],
        securityRule = SecurityRuleNames.SIGNED_IN
    )
    data class RelationshipAttribute_DeleteTag(
        val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val tag: TagRef
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

    @ActionDoc(
        key = "search",
        title = "search",
        description = "Search",
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
        val filters: SearchFilters,
        val fields: SearchFields,
    ) : ModelAction

    // ------------------------------------------------------------------------
    // History
    // ------------------------------------------------------------------------

    @ActionDoc(
        key = "history_versions",
        title = "Versions",
        description = "Lists model released versions",
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
        val modelRef: ModelRef
    ) : ModelAction

    @ActionDoc(
        key = "history_version_changes",
        title = "Version changes",
        description = "Lists changes included in specified version. When no version number is provided, list changes since the last released version.",
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
        val modelRef: ModelRef,
        val version: ModelVersion?
    ) : ModelAction


}
