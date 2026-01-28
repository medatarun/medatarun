package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.ports.exposed.ModelTypeInitializer
import io.medatarun.model.ports.exposed.ModelTypeUpdateCmd
import java.net.URL

sealed interface ModelRepoCmdOnModel : ModelRepoCmd {
    val modelId: ModelId
}

sealed interface ModelRepoCmd {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    data class CreateModel(
        val model: Model
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

    data class UpdateModelHashtagAdd(
        override val modelId: ModelId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel

    data class UpdateModelHashtagDelete(
        override val modelId: ModelId,
        val hashtag: Hashtag
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

    data class UpdateType(
        override val modelId: ModelId,
        val typeId: TypeId,
        val cmd: ModelTypeUpdateCmd
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

    data class UpdateEntity(
        override val modelId: ModelId,
        val entityId: EntityId,
        val cmd: ModelRepoCmdEntityUpdate
    ) : ModelRepoCmdOnModel

    data class UpdateEntityHashtagAdd(
        override val modelId: ModelId,
        val entityId: EntityId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel

    data class UpdateEntityHashtagDelete(
        override val modelId: ModelId,
        val entityId: EntityId,
        val hashtag: Hashtag
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
        val attribute: Attribute
    ) : ModelRepoCmdOnModel

    class DeleteEntityAttribute(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId
    ) : ModelRepoCmdOnModel

    class UpdateEntityAttribute(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val cmd: ModelRepoCmdAttributeUpdate
    ) : ModelRepoCmdOnModel

    data class UpdateEntityAttributeHashtagAdd(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel

    data class UpdateEntityAttributeHashtagDelete(
        override val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel


    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationship(
        override val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelRepoCmdOnModel

    class UpdateRelationship(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val cmd: ModelRepoCmdRelationshipDefUpdate
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipHashtagAdd(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipHashtagDelete(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel

    class DeleteRelationship(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
    ) : ModelRepoCmdOnModel

    class CreateRelationshipAttribute(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attr: Attribute
    ) : ModelRepoCmdOnModel

    class UpdateRelationshipAttribute(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val cmd: ModelRepoCmdAttributeUpdate
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipAttributeHashtagAdd(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel

    data class UpdateRelationshipAttributeHashtagDelete(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
        val hashtag: Hashtag
    ) : ModelRepoCmdOnModel

    class DeleteRelationshipAttribute(
        override val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId,
    ) : ModelRepoCmdOnModel


}