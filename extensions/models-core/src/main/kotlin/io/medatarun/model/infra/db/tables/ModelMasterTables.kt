package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.*
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.Table

object ModelTable : Table("model") {
    val id = text("id").transform(IdTransformer(::ModelId))

    override val primaryKey = PrimaryKey(id)
}

object ModelSnapshotTable : Table("model_snapshot") {
    val id = text("id")
    val modelId = text("model_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::ModelKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val origin = text("origin").transform(ModelOriginTransformer())
    val authority = text("authority").transform(ModelAuthorityTransformer())
    val documentationHome = text("documentation_home").nullable()
    val snapshotKind = text("snapshot_kind")
    val upToRevision = integer("up_to_revision")
    val modelEventReleaseId = text("model_event_release_id").nullable()
    val version = text("version")
    val createdAt = text("created_at")
    val updatedAt = text("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object ModelTagTable : Table("model_tag_snapshot") {
    val modelSnapshotId = text("model_snapshot_id").transform(IdTransformer(::ModelId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object ModelTypeTable : Table("model_type_snapshot") {
    val id = text("id").transform(IdTransformer(::TypeId))
    val lineageId = text("lineage_id").transform(IdTransformer(::TypeId))
    val modelSnapshotId = text("model_snapshot_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::TypeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTable : Table("model_entity_snapshot") {
    val id = text("id").transform(IdTransformer(::EntityId))
    val lineageId = text("lineage_id").transform(IdTransformer(::EntityId))
    val modelSnapshotId = text("model_snapshot_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::EntityKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val identifierAttributeSnapshotId = text("identifier_attribute_snapshot_id").transform(IdTransformer(::AttributeId))
    val origin = text("origin").transform(EntityOriginTransformer())
    val documentationHome = text("documentation_home").nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTagTable : Table("model_entity_tag_snapshot") {
    val entitySnapshotId = text("model_entity_snapshot_id").transform(IdTransformer(::EntityId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object EntityAttributeTable : Table("model_entity_attribute_snapshot") {
    val id = text("id").transform(IdTransformer(::AttributeId))
    val lineageId = text("lineage_id").transform(IdTransformer(::AttributeId))
    val entitySnapshotId = text("model_entity_snapshot_id").transform(IdTransformer(::EntityId))
    val key = text("key").transform(KeyTransformer(::AttributeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val typeSnapshotId = text("model_type_snapshot_id").transform(IdTransformer(::TypeId))
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}

object EntityAttributeTagTable : Table("model_entity_attribute_tag_snapshot") {
    val attributeSnapshotId = text("model_entity_attribute_snapshot_id").transform(IdTransformer(::AttributeId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipTable : Table("model_relationship_snapshot") {
    val id = text("id").transform(IdTransformer(::RelationshipId))
    val lineageId = text("lineage_id").transform(IdTransformer(::RelationshipId))
    val modelSnapshotId = text("model_snapshot_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::RelationshipKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    override val primaryKey = PrimaryKey(id)
}

object RelationshipTagTable : Table("model_relationship_tag_snapshot") {
    val relationshipSnapshotId = text("model_relationship_snapshot_id").transform(IdTransformer(::RelationshipId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipRoleTable : Table("model_relationship_role_snapshot") {
    val id = text("id").transform(IdTransformer(::RelationshipRoleId))
    val lineageId = text("lineage_id").transform(IdTransformer(::RelationshipRoleId))
    val relationshipSnapshotId = text("model_relationship_snapshot_id").transform(IdTransformer(::RelationshipId))
    val key = text("key").transform(KeyTransformer(::RelationshipRoleKey))
    val entitySnapshotId = text("model_entity_snapshot_id").transform(IdTransformer(::EntityId))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val cardinality = text("cardinality")
    override val primaryKey = PrimaryKey(id)
}

object RelationshipAttributeTagTable : Table("model_relationship_attribute_tag_snapshot") {
    val attributeSnapshotId = text("model_relationship_attribute_snapshot_id").transform(IdTransformer(::AttributeId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipAttributeTable : Table("model_relationship_attribute_snapshot") {
    val id = text("id").transform(IdTransformer(::AttributeId))
    val lineageId = text("lineage_id").transform(IdTransformer(::AttributeId))
    val relationshipSnapshotId = text("model_relationship_snapshot_id").transform(IdTransformer(::RelationshipId))
    val key = text("key").transform(KeyTransformer(::AttributeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val typeSnapshotId = text("model_type_snapshot_id").transform(IdTransformer(::TypeId))
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}
