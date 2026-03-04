package io.medatarun.model.infra.db.tables

import org.jetbrains.exposed.v1.core.Table

object RelationshipAttributeTagTable : Table("relationship_attribute_tag") {
    val attributeId = text("attribute_id")
    val tagId = text("tag_id")
}

object RelationshipAttributeTable : Table("relationship_attribute") {
    val id = text("id")
    val relationshipId = text("relationship_id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()
    val typeId = text("type_id")
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}

object ModelTable : Table("model") {
    val id = text("id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()
    val version = text("version")
    val origin = text("origin").nullable()
    val documentationHome = text("documentation_home").nullable()

    override val primaryKey = PrimaryKey(id)
}

object ModelTagTable : Table("model_tag") {
    val modelId = text("model_id")
    val tagId = text("tag_id")
}

object ModelTypeTable : Table("model_type") {
    val id = text("id")
    val modelId = text("model_id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTable : Table("entity") {
    val id = text("id")
    val modelId = text("model_id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()
    val identifierAttributeId = text("identifier_attribute_id")
    val origin = text("origin").nullable()
    val documentationHome = text("documentation_home").nullable()

    override val primaryKey = PrimaryKey(id)
}

object EntityTagTable : Table("entity_tag") {
    val entityId = text("entity_id")
    val tagId = text("tag_id")
}

object EntityAttributeTable : Table("entity_attribute") {
    val id = text("id")
    val entityId = text("entity_id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()
    val typeId = text("type_id")
    val optional = bool("optional")

    override val primaryKey = PrimaryKey(id)
}

object EntityAttributeTagTable : Table("entity_attribute_tag") {
    val attributeId = text("attribute_id")
    val tagId = text("tag_id")
}

object RelationshipTable : Table("relationship") {
    val id = text("id")
    val modelId = text("model_id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

object RelationshipTagTable : Table("relationship_tag") {
    val relationshipId = text("relationship_id")
    val tagId = text("tag_id")
}

object RelationshipRoleTable : Table("relationship_role") {
    val id = text("id")
    val relationshipId = text("relationship_id")
    val key = text("key")
    val entityId = text("entity_id")
    val name = text("name").nullable()
    val cardinality = text("cardinality")

    override val primaryKey = PrimaryKey(id)
}