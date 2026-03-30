package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.*
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID


object DenormModelSearchItemTable : Table("model_search_item_snapshot") {
    val id = text("id")
    val itemType = text("item_type")
    val modelSnapshotId = javaUUID("model_snapshot_id").transform(IdTransformer(::ModelSnapshotId))
    val modelKey = text("model_key").transform(KeyTransformer(::ModelKey))
    val modelLabel = text("model_label")
    val entityId = javaUUID("entity_id").transform(IdTransformer(::EntityId)).nullable()
    val entityKey = text("entity_key").transform(KeyTransformer(::EntityKey)).nullable()
    val entityLabel = text("entity_label").nullable()
    val relationshipId = javaUUID("relationship_id").transform(IdTransformer(::RelationshipId)).nullable()
    val relationshipKey = text("relationship_key").transform(KeyTransformer(::RelationshipKey)).nullable()
    val relationshipLabel = text("relationship_label").nullable()
    val attributeId = javaUUID("attribute_id").transform(IdTransformer(::AttributeId)).nullable()
    val attributeKey = text("attribute_key").transform(KeyTransformer(::AttributeKey)).nullable()
    val attributeLabel = text("attribute_label").nullable()
    val searchText = text("search_text")

    override val primaryKey = PrimaryKey(id)
}

object DenormModelSearchItemTagTable : Table("model_search_item_tag_snapshot") {
    val searchItemId = text("search_item_id")
    val tagId = javaUUID("tag_id").transform(IdTransformer(::TagId))

    override val primaryKey = PrimaryKey(searchItemId, tagId)
}
