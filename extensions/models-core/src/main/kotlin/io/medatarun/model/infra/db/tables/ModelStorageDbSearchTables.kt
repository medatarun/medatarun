package io.medatarun.model.infra.db.tables

import org.jetbrains.exposed.v1.core.Table


object DenormModelSearchItemTable : Table("denorm_model_search_item") {
    val id = text("id")
    val itemType = text("item_type")
    val modelId = text("model_id")
    val modelKey = text("model_key")
    val modelLabel = text("model_label")
    val entityId = text("entity_id").nullable()
    val entityKey = text("entity_key").nullable()
    val entityLabel = text("entity_label").nullable()
    val relationshipId = text("relationship_id").nullable()
    val relationshipKey = text("relationship_key").nullable()
    val relationshipLabel = text("relationship_label").nullable()
    val attributeId = text("attribute_id").nullable()
    val attributeKey = text("attribute_key").nullable()
    val attributeLabel = text("attribute_label").nullable()
    val searchText = text("search_text")

    override val primaryKey = PrimaryKey(id)
}

object DenormModelSearchItemTagTable : Table("denorm_model_search_item_tag") {
    val searchItemId = text("search_item_id")
    val tagId = text("tag_id")

    override val primaryKey = PrimaryKey(searchItemId, tagId)
}
