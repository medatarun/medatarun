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

    data class ImportModel(
        val model: Model,
        val repositoryRef: RepositoryRef = RepositoryRef.Auto
    ) : ModelCmd

    data class UpdateModelName(
        override val modelRef: ModelRef,
        val name: LocalizedText
    ) : ModelCmdOnModel

    data class UpdateModelDescription(
        override val modelRef: ModelRef,
        val description: LocalizedMarkdown?
    ) : ModelCmdOnModel

    data class UpdateModelVersion(
        override val modelRef: ModelRef,
        val version: ModelVersion
    ) : ModelCmdOnModel

    data class UpdateModelDocumentationHome(override val modelRef: ModelRef, val url: URL?) : ModelCmdOnModel

    data class UpdateModelHashtagAdd(override val modelRef: ModelRef, val hashtag: Hashtag) : ModelCmdOnModel
    data class UpdateModelHashtagDelete(override val modelRef: ModelRef, val hashtag: Hashtag) : ModelCmdOnModel

    class CopyModel(override val modelRef: ModelRef, val modelNewKey: ModelKey, val repositoryRef: RepositoryRef = RepositoryRef.Auto) : ModelCmdOnModel

    data class DeleteModel(
        override val modelRef: ModelRef
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(
        override val modelRef: ModelRef,
        val initializer: ModelTypeInitializer
    ) : ModelCmdOnModel

    data class UpdateType(
        override val modelRef: ModelRef,
        val typeId: TypeKey,
        val cmd: ModelTypeUpdateCmd
    ) : ModelCmdOnModel

    data class DeleteType(
        override val modelRef: ModelRef,
        val typeId: TypeKey
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelRef: ModelRef,
        val entityDefInitializer: EntityDefInitializer
    ) : ModelCmdOnModel

    data class UpdateEntityDef(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val cmd: EntityDefUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityDefHashtagAdd(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityDefHashtagDelete(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class DeleteEntityDef(
        override val modelRef: ModelRef,
        val entityKey: EntityKey
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val attributeDefInitializer: AttributeDefInitializer
    ) : ModelCmdOnModel

    class DeleteEntityDefAttributeDef(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey
    ) : ModelCmdOnModel

    class UpdateEntityDefAttributeDef(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagAdd(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagDelete(
        override val modelRef: ModelRef,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelRef: ModelRef,
        val initializer: RelationshipDef
    ) : ModelCmdOnModel

    class UpdateRelationshipDef(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipDefHashtagAdd(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipDefHashtagDelete(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationshipDef(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey
    ) : ModelCmdOnModel


    class CreateRelationshipAttributeDef(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val attr: AttributeDef
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDef(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDefHashtagAdd(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDefHashtagDelete(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationshipAttributeDef(
        override val modelRef: ModelRef,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    ) : ModelCmdOnModel




}

/**
 * Describes a command that can be run on an existing model.
 *
 * A [modelRef] must be provided and the command will not perform and throw a [io.medatarun.model.domain.ModelNotFoundException]
 * if the model doesn't exist.
 */
sealed interface ModelCmdOnModel : ModelCmd {
    val modelRef: ModelRef
}
