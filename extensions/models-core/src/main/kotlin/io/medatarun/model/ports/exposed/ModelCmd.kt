package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*
import io.medatarun.tags.core.domain.TagRef
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
        val version: ModelVersion
    ) : ModelCmd

    data class ImportModel(
        val model: ModelAggregate
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

    data class UpdateModelTagAdd(override val modelRef: ModelRef, val tagRef: TagRef) : ModelCmdOnModel
    data class UpdateModelTagDelete(override val modelRef: ModelRef, val tagRef: TagRef) : ModelCmdOnModel

    class CopyModel(
        override val modelRef: ModelRef,
        val modelNewKey: ModelKey
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

    data class UpdateTypeKey(
        override val modelRef: ModelRef,
        val typeRef: TypeRef,
        val value: TypeKey
    ) : ModelCmdOnModel

    data class UpdateTypeName(
        override val modelRef: ModelRef,
        val typeRef: TypeRef,
        val value: LocalizedText?
    ) : ModelCmdOnModel

    data class UpdateTypeDescription(
        override val modelRef: ModelRef,
        val typeRef: TypeRef,
        val value: LocalizedMarkdown?
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

    data class UpdateEntityKey(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val value: EntityKey
    ) : ModelCmdOnModel

    data class UpdateEntityName(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val value: LocalizedText?
    ) : ModelCmdOnModel

    data class UpdateEntityDescription(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val value: LocalizedMarkdown?
    ) : ModelCmdOnModel

    data class UpdateEntityIdentifierAttribute(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val value: EntityAttributeRef
    ) : ModelCmdOnModel

    data class UpdateEntityDocumentationHome(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val value: URL?
    ) : ModelCmdOnModel

    data class UpdateEntityTagAdd(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val tagRef: TagRef
    ) : ModelCmdOnModel

    data class UpdateEntityTagDelete(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val tagRef: TagRef
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

    class UpdateEntityAttributeKey(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val value: AttributeKey
    ) : ModelCmdOnModel

    class UpdateEntityAttributeName(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val value: LocalizedText?
    ) : ModelCmdOnModel

    class UpdateEntityAttributeDescription(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val value: LocalizedMarkdown?
    ) : ModelCmdOnModel

    class UpdateEntityAttributeType(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val value: TypeRef
    ) : ModelCmdOnModel

    class UpdateEntityAttributeOptional(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val value: Boolean
    ) : ModelCmdOnModel

    data class UpdateEntityAttributeTagAdd(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val tagRef: TagRef
    ) : ModelCmdOnModel

    data class UpdateEntityAttributeTagDelete(
        override val modelRef: ModelRef,
        val entityRef: EntityRef,
        val attributeRef: EntityAttributeRef,
        val tagRef: TagRef
    ) : ModelCmdOnModel

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationship(
        override val modelRef: ModelRef,
        val initializer: RelationshipInitializer
    ) : ModelCmdOnModel

    class UpdateRelationshipKey(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val value: RelationshipKey
    ) : ModelCmdOnModel

    class UpdateRelationshipName(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val value: LocalizedText?
    ) : ModelCmdOnModel

    class UpdateRelationshipDescription(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val value: LocalizedMarkdown?
    ) : ModelCmdOnModel

    class UpdateRelationshipRoleKey(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: RelationshipRoleKey
    ) : ModelCmdOnModel

    class UpdateRelationshipRoleName(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: LocalizedText?
    ) : ModelCmdOnModel

    class UpdateRelationshipRoleEntity(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: EntityRef
    ) : ModelCmdOnModel

    class UpdateRelationshipRoleCardinality(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val relationshipRoleRef: RelationshipRoleRef,
        val value: RelationshipCardinality
    ) : ModelCmdOnModel

    class UpdateRelationshipTagAdd(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val tagRef: TagRef
    ) : ModelCmdOnModel

    class UpdateRelationshipTagDelete(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val tagRef: TagRef
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

    class UpdateRelationshipAttributeName(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val value: LocalizedText?
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeDescription(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val value: LocalizedMarkdown?
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeKey(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val value: AttributeKey
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeType(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val value: TypeRef
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeOptional(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val value: Boolean
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeTagAdd(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val tagRef: TagRef
    ) : ModelCmdOnModel

    class UpdateRelationshipAttributeTagDelete(
        override val modelRef: ModelRef,
        val relationshipRef: RelationshipRef,
        val attributeRef: RelationshipAttributeRef,
        val tagRef: TagRef
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
