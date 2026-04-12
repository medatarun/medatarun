package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.*
import io.medatarun.platform.db.exposed.IdTransformer
import io.medatarun.platform.db.exposed.instant
import io.medatarun.platform.db.exposed.KeyTransformer
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ModelTable : Table("model") {
    val id = javaUUID("id").transform(IdTransformer(::ModelId))

    override val primaryKey = PrimaryKey(id)
}

object ModelSnapshotTable : Table("model_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::ModelSnapshotId))
    val modelId = javaUUID("model_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::ModelKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val origin = text("origin").transform(ModelOriginTransformer())
    val authority = text("authority").transform(ModelAuthorityTransformer())
    val documentationHome = text("documentation_home").nullable()
    val snapshotKind = text("snapshot_kind").transform(ModelSnapshotKindTransformer())
    val upToRevision = integer("up_to_revision")
    val modelEventReleaseId = javaUUID("model_event_release_id").transform(IdTransformer(::ModelEventId)).nullable()
    val version = text("version").transform(ModelVersionTransformer)
    val createdAt = instant("created_at")
    val updatedAt = instant("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object ModelTagTable : Table("model_tag_snapshot") {
    val modelSnapshotId = javaUUID("model_snapshot_id").transform(IdTransformer(::ModelSnapshotId))
    val tagId = javaUUID("tag_id").transform(IdTransformer(::TagId))
}

object ModelTypeTable : Table("model_type_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::TypeSnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::TypeId))
    val modelSnapshotId = javaUUID("model_snapshot_id").transform(IdTransformer(::ModelSnapshotId))
    val key = text("key").transform(KeyTransformer(::TypeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTable : Table("model_entity_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::EntitySnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::EntityId))
    val modelSnapshotId = javaUUID("model_snapshot_id").transform(IdTransformer(::ModelSnapshotId))
    val key = text("key").transform(KeyTransformer(::EntityKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val identifierAttributeSnapshotId = javaUUID("identifier_attribute_snapshot_id").transform(IdTransformer(::AttributeSnapshotId))
    val origin = text("origin").transform(EntityOriginTransformer())
    val documentationHome = text("documentation_home").nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTagTable : Table("model_entity_tag_snapshot") {
    val entitySnapshotId = javaUUID("model_entity_snapshot_id").transform(IdTransformer(::EntitySnapshotId))
    val tagId = javaUUID("tag_id").transform(IdTransformer(::TagId))
}

object EntityAttributeTable : Table("model_entity_attribute_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::AttributeSnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::AttributeId))
    val entitySnapshotId = javaUUID("model_entity_snapshot_id").transform(IdTransformer(::EntitySnapshotId))
    val key = text("key").transform(KeyTransformer(::AttributeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val typeSnapshotId = javaUUID("model_type_snapshot_id").transform(IdTransformer(::TypeSnapshotId))
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}

object EntityPKTable : Table("model_entity_pk_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::EntityPKSnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::EntityPrimaryKeyId))
    val entitySnapshotId = javaUUID("model_entity_snapshot_id").transform(IdTransformer(::EntitySnapshotId))

    override val primaryKey = PrimaryKey(id)
}

object EntityPKAttributeTable : Table("model_entity_pk_attribute_snapshot") {
    val entityPKSnapshotId = javaUUID("model_entity_pk_snapshot_id").transform(IdTransformer(::EntityPKSnapshotId))
    val priority = integer("priority")
    val attributeSnapshotId = javaUUID("model_entity_attribute_snapshot_id").transform(IdTransformer(::AttributeSnapshotId))
}

object BusinessKeyTable : Table("model_business_key_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::BusinessKeySnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::BusinessKeyId))
    val entitySnapshotId = javaUUID("model_entity_snapshot_id").transform(IdTransformer(::EntitySnapshotId))
    val key = text("key").transform(KeyTransformer(::BusinessKeyKey))
    val name = text("name").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

object BusinessKeyAttributeTable : Table("model_business_key_attribute_snapshot") {
    val businessKeySnapshotId = javaUUID("model_business_key_snapshot_id").transform(IdTransformer(::BusinessKeySnapshotId))
    val priority = integer("priority")
    val attributeSnapshotId = javaUUID("model_entity_attribute_snapshot_id").transform(IdTransformer(::AttributeSnapshotId))
}

object EntityAttributeTagTable : Table("model_entity_attribute_tag_snapshot") {
    val attributeSnapshotId = javaUUID("model_entity_attribute_snapshot_id").transform(IdTransformer(::AttributeSnapshotId))
    val tagId = javaUUID("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipTable : Table("model_relationship_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::RelationshipSnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::RelationshipId))
    val modelSnapshotId = javaUUID("model_snapshot_id").transform(IdTransformer(::ModelSnapshotId))
    val key = text("key").transform(KeyTransformer(::RelationshipKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    override val primaryKey = PrimaryKey(id)
}

object RelationshipTagTable : Table("model_relationship_tag_snapshot") {
    val relationshipSnapshotId = javaUUID("model_relationship_snapshot_id").transform(IdTransformer(::RelationshipSnapshotId))
    val tagId = javaUUID("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipRoleTable : Table("model_relationship_role_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::RelationshipRoleSnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::RelationshipRoleId))
    val relationshipSnapshotId = javaUUID("model_relationship_snapshot_id").transform(IdTransformer(::RelationshipSnapshotId))
    val key = text("key").transform(KeyTransformer(::RelationshipRoleKey))
    val entitySnapshotId = javaUUID("model_entity_snapshot_id").transform(IdTransformer(::EntitySnapshotId))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val cardinality = text("cardinality")
    override val primaryKey = PrimaryKey(id)
}

object RelationshipAttributeTagTable : Table("model_relationship_attribute_tag_snapshot") {
    val attributeSnapshotId = javaUUID("model_relationship_attribute_snapshot_id").transform(IdTransformer(::AttributeSnapshotId))
    val tagId = javaUUID("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipAttributeTable : Table("model_relationship_attribute_snapshot") {
    val id = javaUUID("id").transform(IdTransformer(::AttributeSnapshotId))
    val lineageId = javaUUID("lineage_id").transform(IdTransformer(::AttributeId))
    val relationshipSnapshotId = javaUUID("model_relationship_snapshot_id").transform(IdTransformer(::RelationshipSnapshotId))
    val key = text("key").transform(KeyTransformer(::AttributeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val typeSnapshotId = javaUUID("model_type_snapshot_id").transform(IdTransformer(::TypeSnapshotId))
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}
