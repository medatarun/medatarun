package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.ports.exposed.ModelTypeInitializer
import io.medatarun.tags.core.domain.TagId
import java.net.URL

sealed interface ModelRepoCmdOnModel : ModelRepoCmd {
    val modelId: ModelId
}

sealed interface ModelRepoCmd {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    data class CreateModel(
        val model: ModelAggregate
    ) : ModelRepoCmd

    data class UpdateModelName(
        override val modelId: ModelId,
        val name: LocalizedText
    ) : ModelRepoCmdOnModel

    data class UpdateModelDescription(
        override val modelId: ModelId,
        val description: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    data class UpdateModelVersion(
        override val modelId: ModelId,
        val version: ModelVersion
    ) : ModelRepoCmdOnModel

    data class UpdateModelDocumentationHome(
        override val modelId: ModelId,
        val url: URL?
    ) : ModelRepoCmdOnModel

    data class UpdateModelTagAdd(
        override val modelId: ModelId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    data class UpdateModelTagDelete(
        override val modelId: ModelId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    data class DeleteModel(
        override val modelId: ModelId
    ) : ModelRepoCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(
        override val modelId: ModelId,
        val initializer: ModelTypeInitializer
    ) : ModelRepoCmdOnModel

    data class UpdateTypeKey(
        override val modelId: ModelId,
        val typeId: TypeId,
        val value: TypeKey
    ) : ModelRepoCmdOnModel

    data class UpdateTypeName(
        override val modelId: ModelId,
        val typeId: TypeId,
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    data class UpdateTypeDescription(
        override val modelId: ModelId,
        val typeId: TypeId,
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    data class DeleteType(
        override val modelId: ModelId,
        val typeId: TypeId
    ) : ModelRepoCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntity(
        override val modelId: ModelId,
        val entity: EntityInMemory
    ) : ModelRepoCmdOnModel

    data class UpdateEntityKey(
        override val modelId: ModelId,
        val entityId: EntityId,
        val value: EntityKey
    ) : ModelRepoCmdOnModel

    data class UpdateEntityName(
        override val modelId: ModelId,
        val entityId: EntityId,
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    data class UpdateEntityDescription(
        override val modelId: ModelId,
        val entityId: EntityId,
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    data class UpdateEntityIdentifierAttribute(
        override val modelId: ModelId,
        val entityId: EntityId,
        val value: AttributeId
    ) : ModelRepoCmdOnModel

    data class UpdateEntityDocumentationHome(
        override val modelId: ModelId,
        val entityId: EntityId,
        val value: URL?
    ) : ModelRepoCmdOnModel

    data class UpdateEntityTagAdd(
        override val modelId: ModelId,
        val entityId: EntityId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    data class UpdateEntityTagDelete(
        override val modelId: ModelId,
        val entityId: EntityId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    data class DeleteEntity(
        override val modelId: ModelId,
        val entityId: EntityId
    ) : ModelRepoCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityAttribute(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val key: AttributeKey,
        val name: LocalizedText?,
        val description: LocalizedMarkdown?,
        val typeId: TypeId,
        val optional: Boolean
    ) : ModelRepoCmdOnModel

    class DeleteEntityAttribute(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId
    ) : ModelRepoCmdOnModel

    class UpdateEntityAttributeKey(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val value: AttributeKey
    ) : ModelRepoCmdOnModel

    class UpdateEntityAttributeName(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    class UpdateEntityAttributeDescription(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    class UpdateEntityAttributeType(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val value: TypeId
    ) : ModelRepoCmdOnModel

    class UpdateEntityAttributeOptional(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val value: Boolean
    ) : ModelRepoCmdOnModel

    data class UpdateEntityAttributeTagAdd(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    data class UpdateEntityAttributeTagDelete(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel


    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationship(
        override val modelId: ModelId,
        val initializer: Relationship
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipKey(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val value: RelationshipKey
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipName(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipDescription(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipRoleKey(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val relationshipRoleId: RelationshipRoleId,
        val value: RelationshipRoleKey
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipRoleName(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val relationshipRoleId: RelationshipRoleId,
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipRoleEntity(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val relationshipRoleId: RelationshipRoleId,
        val value: EntityId
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipRoleCardinality(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val relationshipRoleId: RelationshipRoleId,
        val value: RelationshipCardinality
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipTagAdd(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipTagDelete(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    class DeleteRelationship(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
    ) : ModelRepoCmdOnModel

    class CreateRelationshipAttribute(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val key: AttributeKey,
        val name: LocalizedText?,
        val description: LocalizedMarkdown?,
        val typeId: TypeId,
        val optional: Boolean,
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipAttributeName(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipAttributeDescription(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipAttributeKey(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val value: AttributeKey
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipAttributeType(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val value: TypeId
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipAttributeOptional(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val value: Boolean
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipAttributeTagAdd(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipAttributeTagDelete(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    class DeleteRelationshipAttribute(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
    ) : ModelRepoCmdOnModel


}
