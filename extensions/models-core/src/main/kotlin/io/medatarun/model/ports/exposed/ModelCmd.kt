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

    class CopyModel(
        override val modelRef: ModelRef,
        val modelNewKey: ModelKey,
        val repositoryRef: RepositoryRef = RepositoryRef.Auto
    ) : ModelCmdOnModel

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
        val typeRef: TypeRef,
        val cmd: ModelTypeUpdateCmd
    ) : ModelCmdOnModel

    data class DeleteType(
        override val modelRef: ModelRef,
        val typeRef: TypeRef,
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntity(
        override val modelRef: ModelRef,
        val entityInitializer: EntityInitializer
    ) : ModelCmdOnModel

    data class UpdateEntity(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val cmd: EntityUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityHashtagAdd(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityHashtagDelete(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class DeleteEntity(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityAttribute(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeInitializer: AttributeInitializer
    ) : ModelCmdOnModel

    class DeleteEntityAttribute(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
    ) : ModelCmdOnModel

    class UpdateEntityAttribute(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val cmd: AttributeUpdateCmd
    ) : ModelCmdOnModel

    data class UpdateEntityAttributeHashtagAdd(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    data class UpdateEntityAttributeHashtagDelete(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationship(
        override val modelRef: ModelRef,
        val initializer: RelationshipInitializer
    ) : ModelCmdOnModel

    class UpdateRelationship(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val cmd: RelationshipUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipHashtagAdd(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipHashtagDelete(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationship(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef
    ) : ModelCmdOnModel


    class CreateRelationshipAttribute(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attr: AttributeInitializer
    ) : ModelCmdOnModel

    class UpdateRelationshipAttribute(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val cmd: AttributeUpdateCmd
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeHashtagAdd(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeHashtagDelete(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val hashtag: Hashtag
    ) : ModelCmdOnModel

    class DeleteRelationshipAttribute(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
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
