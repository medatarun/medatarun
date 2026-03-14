@file:UseContextualSerialization(
    AttributeId::class,
    AttributeKey::class,
    EntityId::class,
    EntityKey::class,
    EntityOrigin::class,
    LocalizedMarkdown::class,
    LocalizedText::class,
    Model::class,
    ModelAuthority::class,
    ModelId::class,
    ModelKey::class,
    ModelOrigin::class,
    ModelTypeInitializer::class,
    ModelVersion::class,
    RelationshipCardinality::class,
    RelationshipId::class,
    RelationshipKey::class,
    RelationshipRoleId::class,
    RelationshipRoleKey::class,
    TagId::class,
    TypeId::class,
    TypeKey::class,
    URL::class,
)

package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelEventContract
import io.medatarun.model.ports.exposed.ModelTypeInitializer
import io.medatarun.tags.core.domain.TagId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.net.URL

@Serializable
sealed interface ModelRepoCmdOnModel : ModelRepoCmd {
    val modelId: ModelId
}

@Serializable
sealed interface ModelRepoCmd {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    @Serializable
    @ModelEventContract(eventType = "model_aggregate_stored", eventVersion = 1)
    data class StoreModelAggregate(
        @SerialName("model")
        val model: StoreModelAggregateModel,
        @SerialName("types")
        val types: List<StoreModelAggregateType>,
        @SerialName("entities")
        val entities: List<StoreModelAggregateEntity>,
        @SerialName("entity_attributes")
        val entityAttributes: List<StoreModelAggregateEntityAttribute>,
        @SerialName("relationships")
        val relationships: List<StoreModelAggregateRelationship>,
        @SerialName("relationship_attributes")
        val relationshipAttributes: List<StoreModelAggregateRelationshipAttribute>,
    ) : ModelRepoCmd

    @Serializable
    @ModelEventContract(eventType = "model_created", eventVersion = 1)
    data class CreateModel(
        @SerialName("model")
        val model: Model
    ) : ModelRepoCmd

    @Serializable
    @ModelEventContract(eventType = "model_name_updated", eventVersion = 1)
    data class UpdateModelName(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("name")
        val name: LocalizedText
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_key_updated", eventVersion = 1)
    data class UpdateModelKey(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("key")
        val key: ModelKey
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_description_updated", eventVersion = 1)
    data class UpdateModelDescription(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_authority_updated", eventVersion = 1)
    data class UpdateModelAuthority(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("authority")
        val authority: ModelAuthority
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_version_updated", eventVersion = 1)
    data class UpdateModelVersion(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("version")
        val version: ModelVersion
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_documentation_home_updated", eventVersion = 1)
    data class UpdateModelDocumentationHome(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("url")
        val url: URL?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_tag_added", eventVersion = 1)
    data class UpdateModelTagAdd(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_tag_deleted", eventVersion = 1)
    data class UpdateModelTagDelete(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "model_deleted", eventVersion = 1)
    data class DeleteModel(
        @SerialName("model_id")
        override val modelId: ModelId
    ) : ModelRepoCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    @Serializable
    @ModelEventContract(eventType = "type_created", eventVersion = 1)
    data class CreateType(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("initializer")
        val initializer: ModelTypeInitializer
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "type_key_updated", eventVersion = 1)
    data class UpdateTypeKey(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("type_id")
        val typeId: TypeId,
        @SerialName("value")
        val value: TypeKey
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "type_name_updated", eventVersion = 1)
    data class UpdateTypeName(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("type_id")
        val typeId: TypeId,
        @SerialName("value")
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "type_description_updated", eventVersion = 1)
    data class UpdateTypeDescription(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("type_id")
        val typeId: TypeId,
        @SerialName("value")
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "type_deleted", eventVersion = 1)
    data class DeleteType(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("type_id")
        val typeId: TypeId
    ) : ModelRepoCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    @Serializable
    @ModelEventContract(eventType = "entity_created", eventVersion = 1)
    data class CreateEntity(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("key")
        val key: EntityKey,
        @SerialName("name")
        val name: LocalizedText?,
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @SerialName("documentation_home")
        val documentationHome: URL?,
        @SerialName("origin")
        val origin: EntityOrigin,
        @SerialName("identity_attribute_id")
        val identityAttributeId: AttributeId,
        @SerialName("identity_attribute_key")
        val identityAttributeKey: AttributeKey,
        @SerialName("identity_attribute_type_id")
        val identityAttributeTypeId: TypeId,
        @SerialName("identity_attribute_name")
        val identityAttributeName: LocalizedText?,
        @SerialName("identity_attribute_description")
        val identityAttributeDescription: LocalizedMarkdown?,
        @SerialName("identity_attribute_optional")
        val identityAttributeIdOptional: Boolean,

    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_key_updated", eventVersion = 1)
    data class UpdateEntityKey(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("value")
        val value: EntityKey
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_name_updated", eventVersion = 1)
    data class UpdateEntityName(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("value")
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_description_updated", eventVersion = 1)
    data class UpdateEntityDescription(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("value")
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_identifier_attribute_updated", eventVersion = 1)
    data class UpdateEntityIdentifierAttribute(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("value")
        val value: AttributeId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_documentation_home_updated", eventVersion = 1)
    data class UpdateEntityDocumentationHome(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("value")
        val value: URL?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_tag_added", eventVersion = 1)
    data class UpdateEntityTagAdd(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_tag_deleted", eventVersion = 1)
    data class UpdateEntityTagDelete(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_deleted", eventVersion = 1)
    data class DeleteEntity(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId
    ) : ModelRepoCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_created", eventVersion = 1)
    data class CreateEntityAttribute(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("key")
        val key: AttributeKey,
        @SerialName("name")
        val name: LocalizedText?,
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @SerialName("type_id")
        val typeId: TypeId,
        @SerialName("optional")
        val optional: Boolean
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_deleted", eventVersion = 1)
    data class DeleteEntityAttribute(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_key_updated", eventVersion = 1)
    data class UpdateEntityAttributeKey(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: AttributeKey
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_name_updated", eventVersion = 1)
    data class UpdateEntityAttributeName(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_description_updated", eventVersion = 1)
    data class UpdateEntityAttributeDescription(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_type_updated", eventVersion = 1)
    data class UpdateEntityAttributeType(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: TypeId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_optional_updated", eventVersion = 1)
    data class UpdateEntityAttributeOptional(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: Boolean
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_tag_added", eventVersion = 1)
    data class UpdateEntityAttributeTagAdd(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "entity_attribute_tag_deleted", eventVersion = 1)
    data class UpdateEntityAttributeTagDelete(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel


    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    @Serializable
    @ModelEventContract(eventType = "relationship_created", eventVersion = 1)
    data class CreateRelationship(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("key")
        val key: RelationshipKey,
        @SerialName("name")
        val name: LocalizedText?,
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @SerialName("roles")
        val roles: List<RelationshipRoleInitializer>,
    ) : ModelRepoCmdOnModel

    @Serializable
    data class RelationshipRoleInitializer(
        @SerialName("id")
        val id: RelationshipRoleId,
        @SerialName("key")
        val key: RelationshipRoleKey,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("name")
        val name: LocalizedText?,
        @SerialName("cardinality")
        val cardinality: RelationshipCardinality,
    )

    @Serializable
    @ModelEventContract(eventType = "relationship_key_updated", eventVersion = 1)
    data class UpdateRelationshipKey(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("value")
        val value: RelationshipKey
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_name_updated", eventVersion = 1)
    data class UpdateRelationshipName(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("value")
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_description_updated", eventVersion = 1)
    data class UpdateRelationshipDescription(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("value")
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_role_created", eventVersion = 1)
    data class CreateRelationshipRole(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("relationship_role_id")
        val relationshipRoleId: RelationshipRoleId,
        @SerialName("key")
        val key: RelationshipRoleKey,
        @SerialName("entity_id")
        val entityId: EntityId,
        @SerialName("name")
        val name: LocalizedText?,
        @SerialName("cardinality")
        val cardinality: RelationshipCardinality
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_role_key_updated", eventVersion = 1)
    data class UpdateRelationshipRoleKey(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("relationship_role_id")
        val relationshipRoleId: RelationshipRoleId,
        @SerialName("value")
        val value: RelationshipRoleKey
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_role_name_updated", eventVersion = 1)
    data class UpdateRelationshipRoleName(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("relationship_role_id")
        val relationshipRoleId: RelationshipRoleId,
        @SerialName("value")
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_role_entity_updated", eventVersion = 1)
    data class UpdateRelationshipRoleEntity(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("relationship_role_id")
        val relationshipRoleId: RelationshipRoleId,
        @SerialName("value")
        val value: EntityId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_role_cardinality_updated", eventVersion = 1)
    data class UpdateRelationshipRoleCardinality(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("relationship_role_id")
        val relationshipRoleId: RelationshipRoleId,
        @SerialName("value")
        val value: RelationshipCardinality
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_tag_added", eventVersion = 1)
    data class UpdateRelationshipTagAdd(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_tag_deleted", eventVersion = 1)
    data class UpdateRelationshipTagDelete(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_deleted", eventVersion = 1)
    data class DeleteRelationship(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_role_deleted", eventVersion = 1)
    data class DeleteRelationshipRole(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("relationship_role_id")
        val relationshipRoleId: RelationshipRoleId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_created", eventVersion = 1)
    data class CreateRelationshipAttribute(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("key")
        val key: AttributeKey,
        @SerialName("name")
        val name: LocalizedText?,
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @SerialName("type_id")
        val typeId: TypeId,
        @SerialName("optional")
        val optional: Boolean,
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_name_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeName(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: LocalizedText?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_description_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeDescription(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: LocalizedMarkdown?
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_key_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeKey(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: AttributeKey
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_type_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeType(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: TypeId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_optional_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeOptional(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("value")
        val value: Boolean
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_tag_added", eventVersion = 1)
    data class UpdateRelationshipAttributeTagAdd(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_tag_deleted", eventVersion = 1)
    data class UpdateRelationshipAttributeTagDelete(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
        @SerialName("tag_id")
        val tagId: TagId
    ) : ModelRepoCmdOnModel

    @Serializable
    @ModelEventContract(eventType = "relationship_attribute_deleted", eventVersion = 1)
    data class DeleteRelationshipAttribute(
        @SerialName("model_id")
        override val modelId: ModelId,
        @SerialName("relationship_id")
        val relationshipId: RelationshipId,
        @SerialName("attribute_id")
        val attributeId: AttributeId,
    ) : ModelRepoCmdOnModel


}
