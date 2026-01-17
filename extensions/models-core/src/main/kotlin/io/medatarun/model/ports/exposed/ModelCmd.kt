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
        val modelKey: ModelKey,
        val name: LocalizedText,
        val description: LocalizedMarkdown?,
        val version: ModelVersion,
        val repositoryRef: RepositoryRef = RepositoryRef.Auto
    ) : ModelCmd

    data class ImportModel(val model: Model, val repositoryRef: RepositoryRef = RepositoryRef.Auto) : ModelCmd

    data class UpdateModelName(
        override val modelKey: ModelKey,
        val name: LocalizedText
    ) : ModelCmdOnModel

    data class UpdateModelDescription(
        override val modelKey: ModelKey,
        val description: LocalizedMarkdown?
    ) : ModelCmdOnModel

    data class UpdateModelVersion(
        override val modelKey: ModelKey,
        val version: ModelVersion
    ) : ModelCmdOnModel

    data class UpdateModelDocumentationHome(override val modelKey: ModelKey, val url: URL?) : ModelCmdOnModel

    data class UpdateModelHashtagAdd(override val modelKey: ModelKey, val hashtag: Hashtag) : ModelCmdOnModel
    data class UpdateModelHashtagDelete(override val modelKey: ModelKey, val hashtag: Hashtag) : ModelCmdOnModel

    class CopyModel(override val modelKey: ModelKey, val modelNewKey: ModelKey, val repositoryRef: RepositoryRef = RepositoryRef.Auto) : ModelCmdOnModel

    data class DeleteModel(
        override val modelKey: ModelKey
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(
        override val modelKey: ModelKey,
        val initializer: ModelTypeInitializer
    ) : ModelCmdOnModel

    data class UpdateType(
        override val modelKey: ModelKey,
        val typeId: TypeKey,
        val cmd: ModelTypeUpdateCmd
    ) : ModelCmdOnModel

    data class DeleteType(
        override val modelKey: ModelKey,
        val typeId: TypeKey
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelKey: ModelKey,
        val entityDefInitializer: EntityDefInitializer
    ) : ModelCmdOnModel

    data class UpdateEntityDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val cmd: EntityDefUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityDefHashtagAdd(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityDefHashtagDelete(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class DeleteEntityDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeDefInitializer: AttributeDefInitializer
    ) : ModelCmdOnModel

    class DeleteEntityDefAttributeDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey
    ) : ModelCmdOnModel

    class UpdateEntityDefAttributeDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagAdd(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagDelete(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelKey: ModelKey,
        val initializer: RelationshipDef
    ) : ModelCmdOnModel

    class UpdateRelationshipDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipDefHashtagAdd(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipDefHashtagDelete(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationshipDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey
    ) : ModelCmdOnModel


    class CreateRelationshipAttributeDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attr: AttributeDef
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDefHashtagAdd(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDefHashtagDelete(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationshipAttributeDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    ) : ModelCmdOnModel




}

/**
 * Describes a command that can be run on an existing model.
 *
 * A [modelKey] must be provided and the command will not perform and throw a [io.medatarun.model.domain.ModelNotFoundException]
 * if the model doesn't exist.
 */
sealed interface ModelCmdOnModel : ModelCmd {
    val modelKey: ModelKey
}
