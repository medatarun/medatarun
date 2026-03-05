package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.*
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.Table

object ModelTable : Table("model") {
    val id = text("id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::ModelKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val version = text("version")
    val origin = text("origin").nullable()
    val documentationHome = text("documentation_home").nullable()

    override val primaryKey = PrimaryKey(id)
}

object ModelTagTable : Table("model_tag") {
    val modelId = text("model_id").transform(IdTransformer(::ModelId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object ModelTypeTable : Table("model_type") {
    val id = text("id").transform(IdTransformer(::TypeId))
    val modelId = text("model_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::TypeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTable : Table("entity") {
    val id = text("id").transform(IdTransformer(::EntityId))
    val modelId = text("model_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::EntityKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val identifierAttributeId = text("identifier_attribute_id").transform(IdTransformer(::AttributeId))
    val origin = text("origin").nullable()
    val documentationHome = text("documentation_home").nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTagTable : Table("entity_tag") {
    val entityId = text("entity_id").transform(IdTransformer(::EntityId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object EntityAttributeTable : Table("entity_attribute") {
    val id = text("id").transform(IdTransformer(::AttributeId))
    val entityId = text("entity_id").transform(IdTransformer(::EntityId))
    val key = text("key").transform(KeyTransformer(::AttributeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val typeId = text("type_id").transform(IdTransformer(::TypeId))
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}

object EntityAttributeTagTable : Table("entity_attribute_tag") {
    val attributeId = text("attribute_id").transform(IdTransformer(::AttributeId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipTable : Table("relationship") {
    val id = text("id").transform(IdTransformer(::RelationshipId))
    val modelId = text("model_id").transform(IdTransformer(::ModelId))
    val key = text("key").transform(KeyTransformer(::RelationshipKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    override val primaryKey = PrimaryKey(id)
}

object RelationshipTagTable : Table("relationship_tag") {
    val relationshipId = text("relationship_id").transform(IdTransformer(::RelationshipId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipRoleTable : Table("relationship_role") {
    val id = text("id").transform(IdTransformer(::RelationshipRoleId))
    val relationshipId = text("relationship_id").transform(IdTransformer(::RelationshipId))
    val key = text("key").transform(KeyTransformer(::RelationshipRoleKey))
    val entityId = text("entity_id").transform(IdTransformer(::EntityId))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val cardinality = text("cardinality")
    override val primaryKey = PrimaryKey(id)
}

object RelationshipAttributeTagTable : Table("relationship_attribute_tag") {
    val attributeId = text("attribute_id").transform(IdTransformer(::AttributeId))
    val tagId = text("tag_id").transform(IdTransformer(::TagId))
}

object RelationshipAttributeTable : Table("relationship_attribute") {
    val id = text("id").transform(IdTransformer(::AttributeId))
    val relationshipId = text("relationship_id").transform(IdTransformer(::RelationshipId))
    val key = text("key").transform(KeyTransformer(::AttributeKey))
    val name = text("name").transform(LocalizedTextTransformer()).nullable()
    val description = text("description").transform(LocalizedMarkdownTransformer()).nullable()
    val typeId = text("type_id").transform(IdTransformer(::TypeId))
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}

