package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import io.medatarun.storage.eventsourcing.StorageEventContract
import io.medatarun.tags.core.domain.TagId
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL

/**
 * Current storage commands
 */
@Serializable
sealed interface ModelStorageCmd: ModelStorageCmdAnyVersion {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    @Serializable
    @StorageEventContract(eventType = "model_aggregate_stored", eventVersion = 1)
    data class StoreModelAggregate(
        @SerialName("model")
        val model: StoreModelAggregateModel,
        @SerialName("types")
        val types: List<StoreModelAggregateType>,
        @SerialName("entities")
        val entities: List<StoreModelAggregateEntity>,
        @SerialName("entityAttributes")
        val entityAttributes: List<StoreModelAggregateEntityAttribute>,
        @SerialName("relationships")
        val relationships: List<StoreModelAggregateRelationship>,
        @SerialName("relationshipAttributes")
        val relationshipAttributes: List<StoreModelAggregateRelationshipAttribute>,
    ) : ModelStorageCmd

    @Serializable
    @StorageEventContract(eventType = "model_created", eventVersion = 1)
    data class CreateModel(
        @Contextual
        @SerialName("id")
        val id: ModelId,
        @Contextual
        @SerialName("key")
        val key: ModelKey,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @Contextual
        @SerialName("version")
        val version: ModelVersion,
        @Contextual
        @SerialName("origin")
        val origin: ModelOrigin,
        @Contextual
        @SerialName("authority")
        val authority: ModelAuthority,
        @Contextual
        @SerialName("documentationHome")
        val documentationHome: URL?
    ) : ModelStorageCmd

    @Serializable
    @StorageEventContract(eventType = "model_name_updated", eventVersion = 1)
    data class UpdateModelName(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_key_updated", eventVersion = 1)
    data class UpdateModelKey(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("key")
        val key: ModelKey
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_description_updated", eventVersion = 1)
    data class UpdateModelDescription(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_authority_updated", eventVersion = 1)
    data class UpdateModelAuthority(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("authority")
        val authority: ModelAuthority
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_release", eventVersion = 1)
    data class ModelRelease(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("version")
        val version: ModelVersion
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_documentation_home_updated", eventVersion = 1)
    data class UpdateModelDocumentationHome(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("documentationHome")
        val documentationHome: URL?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_tag_added", eventVersion = 1)
    data class UpdateModelTagAdd(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_tag_deleted", eventVersion = 1)
    data class UpdateModelTagDelete(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "model_deleted", eventVersion = 1)
    data class DeleteModel(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId
    ) : ModelStorageCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    @Serializable
    @StorageEventContract(eventType = "type_created", eventVersion = 1)
    data class CreateType(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId,
        @Contextual
        @SerialName("key")
        val key: TypeKey,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "type_key_updated", eventVersion = 1)
    data class UpdateTypeKey(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId,
        @Contextual
        @SerialName("key")
        val key: TypeKey
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "type_name_updated", eventVersion = 1)
    data class UpdateTypeName(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "type_description_updated", eventVersion = 1)
    data class UpdateTypeDescription(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "type_deleted", eventVersion = 1)
    data class DeleteType(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId
    ) : ModelStorageCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    @Serializable
    @StorageEventContract(eventType = "entity_created", eventVersion = 2)
    data class CreateEntity(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("key")
        val key: EntityKey,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @Contextual
        @SerialName("documentationHome")
        val documentationHome: URL?,
        @Contextual
        @SerialName("origin")
        val origin: EntityOrigin,
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_key_updated", eventVersion = 1)
    data class UpdateEntityKey(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("key")
        val key: EntityKey
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_name_updated", eventVersion = 1)
    data class UpdateEntityName(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_description_updated", eventVersion = 1)
    data class UpdateEntityDescription(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_primary_key_set", eventVersion = 1)
    data class Entity_PrimaryKey_Set(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeIds")
        val attributeIds: List<@Contextual AttributeId>
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_identifier_attribute_updated", eventVersion = 1)
    data class UpdateEntityIdentifierAttribute(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("identifierAttributeId")
        val identifierAttributeId: AttributeId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_documentation_home_updated", eventVersion = 1)
    data class UpdateEntityDocumentationHome(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("documentationHome")
        val documentationHome: URL?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_tag_added", eventVersion = 1)
    data class UpdateEntityTagAdd(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_tag_deleted", eventVersion = 1)
    data class UpdateEntityTagDelete(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_deleted", eventVersion = 1)
    data class DeleteEntity(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId
    ) : ModelStorageCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_created", eventVersion = 1)
    data class CreateEntityAttribute(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("key")
        val key: AttributeKey,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId,
        @SerialName("optional")
        val optional: Boolean
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_deleted", eventVersion = 1)
    data class DeleteEntityAttribute(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_key_updated", eventVersion = 1)
    data class UpdateEntityAttributeKey(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("key")
        val key: AttributeKey
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_name_updated", eventVersion = 1)
    data class UpdateEntityAttributeName(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_description_updated", eventVersion = 1)
    data class UpdateEntityAttributeDescription(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_type_updated", eventVersion = 1)
    data class UpdateEntityAttributeType(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_optional_updated", eventVersion = 1)
    data class UpdateEntityAttributeOptional(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @SerialName("optional")
        val optional: Boolean
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_tag_added", eventVersion = 1)
    data class UpdateEntityAttributeTagAdd(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "entity_attribute_tag_deleted", eventVersion = 1)
    data class UpdateEntityAttributeTagDelete(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel


    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    @Serializable
    @StorageEventContract(eventType = "relationship_created", eventVersion = 1)
    data class CreateRelationship(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("key")
        val key: RelationshipKey,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @SerialName("roles")
        val roles: List<RelationshipRoleInitializer>,
    ) : ModelStorageCmdOnModel

    @Serializable
    data class RelationshipRoleInitializer(
        @Contextual
        @SerialName("id")
        val id: RelationshipRoleId,
        @Contextual
        @SerialName("key")
        val key: RelationshipRoleKey,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("cardinality")
        val cardinality: RelationshipCardinality,
    )

    @Serializable
    @StorageEventContract(eventType = "relationship_key_updated", eventVersion = 1)
    data class UpdateRelationshipKey(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("key")
        val key: RelationshipKey
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_name_updated", eventVersion = 1)
    data class UpdateRelationshipName(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_description_updated", eventVersion = 1)
    data class UpdateRelationshipDescription(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_role_created", eventVersion = 1)
    data class CreateRelationshipRole(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("relationshipRoleId")
        val relationshipRoleId: RelationshipRoleId,
        @Contextual
        @SerialName("key")
        val key: RelationshipRoleKey,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("cardinality")
        val cardinality: RelationshipCardinality
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_role_key_updated", eventVersion = 1)
    data class UpdateRelationshipRoleKey(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("relationshipRoleId")
        val relationshipRoleId: RelationshipRoleId,
        @Contextual
        @SerialName("key")
        val key: RelationshipRoleKey
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_role_name_updated", eventVersion = 1)
    data class UpdateRelationshipRoleName(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("relationshipRoleId")
        val relationshipRoleId: RelationshipRoleId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_role_entity_updated", eventVersion = 1)
    data class UpdateRelationshipRoleEntity(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("relationshipRoleId")
        val relationshipRoleId: RelationshipRoleId,
        @Contextual
        @SerialName("entityId")
        val entityId: EntityId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_role_cardinality_updated", eventVersion = 1)
    data class UpdateRelationshipRoleCardinality(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("relationshipRoleId")
        val relationshipRoleId: RelationshipRoleId,
        @Contextual
        @SerialName("cardinality")
        val cardinality: RelationshipCardinality
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_tag_added", eventVersion = 1)
    data class UpdateRelationshipTagAdd(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_tag_deleted", eventVersion = 1)
    data class UpdateRelationshipTagDelete(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_deleted", eventVersion = 1)
    data class DeleteRelationship(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_role_deleted", eventVersion = 1)
    data class DeleteRelationshipRole(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("relationshipRoleId")
        val relationshipRoleId: RelationshipRoleId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_created", eventVersion = 1)
    data class CreateRelationshipAttribute(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("key")
        val key: AttributeKey,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId,
        @SerialName("optional")
        val optional: Boolean,
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_name_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeName(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("name")
        val name: LocalizedText?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_description_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeDescription(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("description")
        val description: LocalizedMarkdown?
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_key_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeKey(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("key")
        val key: AttributeKey
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_type_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeType(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("typeId")
        val typeId: TypeId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_optional_updated", eventVersion = 1)
    data class UpdateRelationshipAttributeOptional(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @SerialName("optional")
        val optional: Boolean
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_tag_added", eventVersion = 1)
    data class UpdateRelationshipAttributeTagAdd(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_tag_deleted", eventVersion = 1)
    data class UpdateRelationshipAttributeTagDelete(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
        @Contextual
        @SerialName("tagId")
        val tagId: TagId
    ) : ModelStorageCmdOnModel

    @Serializable
    @StorageEventContract(eventType = "relationship_attribute_deleted", eventVersion = 1)
    data class DeleteRelationshipAttribute(
        @Contextual
        @SerialName("modelId")
        override val modelId: ModelId,
        @Contextual
        @SerialName("relationshipId")
        val relationshipId: RelationshipId,
        @Contextual
        @SerialName("attributeId")
        val attributeId: AttributeId,
    ) : ModelStorageCmdOnModel


}
