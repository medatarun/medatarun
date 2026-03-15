package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.Table


object DenormModelSearchItemTable : Table("model_search_item_snapshot") {
    val id = text("id")
    val itemType = text("item_type")
    val modelSnapshotId = text("model_snapshot_id").transform(IdTransformer(::ModelId))
    val modelKey = text("model_key").transform(KeyTransformer(::ModelKey))
    val modelLabel = text("model_label")
    val entityId = text("entity_id").transform(IdTransformer(::EntityId)).nullable()
    val entityKey = text("entity_key").transform(KeyTransformer(::EntityKey)).nullable()
    val entityLabel = text("entity_label").nullable()
    val relationshipId = text("relationship_id").transform(IdTransformer(::RelationshipId)).nullable()
    val relationshipKey = text("relationship_key").transform(KeyTransformer(::RelationshipKey)).nullable()
    val relationshipLabel = text("relationship_label").nullable()
    val attributeId = text("attribute_id").transform(IdTransformer(::AttributeId)).nullable()
    val attributeKey = text("attribute_key").transform(KeyTransformer(::AttributeKey)).nullable()
    val attributeLabel = text("attribute_label").nullable()
    val searchText = text("search_text")

    override val primaryKey = PrimaryKey(id)
}

object DenormModelSearchItemTagTable : Table("model_search_item_tag_snapshot") {
    val searchItemId = text("search_item_id")
    val tagId = text("tag_id").transform(IdTransformer(::TagId))

    override val primaryKey = PrimaryKey(searchItemId, tagId)
}
