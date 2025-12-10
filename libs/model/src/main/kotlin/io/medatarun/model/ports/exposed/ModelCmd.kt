package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*
import io.medatarun.model.ports.needs.RepositoryRef
import java.net.URL

/**
 * All possible commands that can be used to manage models, entity definitions, attributes,
 * relationships definitions and their attributes.
 *
 * See that as the core business interface to manage models.
 */
sealed interface ModelCmd {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    data class CreateModel(
        val id: ModelId,
        val name: LocalizedText,
        val description: LocalizedMarkdown?,
        val version: ModelVersion,
        val repositoryRef: RepositoryRef = RepositoryRef.Auto
    ) : ModelCmd

    data class ImportModel(val model: Model, val repositoryRef: RepositoryRef = RepositoryRef.Auto) : ModelCmd

    data class UpdateModelName(
        override val modelId: ModelId,
        val name: LocalizedTextNotLocalized
    ) : ModelCmdOnModel

    data class UpdateModelDescription(
        override val modelId: ModelId,
        val description: LocalizedTextNotLocalized?
    ) : ModelCmdOnModel

    data class UpdateModelVersion(
        override val modelId: ModelId,
        val version: ModelVersion
    ) : ModelCmdOnModel

    data class UpdateModelDocumentationHome(override val modelId: ModelId, val url: URL?) : ModelCmdOnModel

    data class UpdateModelHashtagAdd(override val modelId: ModelId, val hashtag: Hashtag) : ModelCmdOnModel
    data class UpdateModelHashtagDelete(override val modelId: ModelId, val hashtag: Hashtag) : ModelCmdOnModel

    data class DeleteModel(
        override val modelId: ModelId
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(
        override val modelId: ModelId,
        val initializer: ModelTypeInitializer
    ) : ModelCmdOnModel

    data class UpdateType(
        override val modelId: ModelId,
        val typeId: ModelTypeId,
        val cmd: ModelTypeUpdateCmd
    ) : ModelCmdOnModel

    data class DeleteType(
        override val modelId: ModelId,
        val typeId: ModelTypeId
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelId: ModelId,
        val entityDefInitializer: EntityDefInitializer
    ) : ModelCmdOnModel

    data class UpdateEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val cmd: EntityDefUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityDefHashtagAdd(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityDefHashtagDelete(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class DeleteEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefInitializer: AttributeDefInitializer
    ) : ModelCmdOnModel

    class DeleteEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId
    ) : ModelCmdOnModel

    class UpdateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagAdd(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagDelete(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelCmdOnModel

    class UpdateRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipDefHashtagAdd(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipDefHashtagDelete(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId
    ) : ModelCmdOnModel


    class CreateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attr: AttributeDef
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDefHashtagAdd(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDefHashtagDelete(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId
    ) : ModelCmdOnModel


}

/**
 * Describes a command that can be run on an existing model.
 *
 * A [modelId] must be provided and the command will not perform and throw a [io.medatarun.model.domain.ModelNotFoundException]
 * if the model doesn't exist.
 */
sealed interface ModelCmdOnModel : ModelCmd {
    val modelId: ModelId
}
