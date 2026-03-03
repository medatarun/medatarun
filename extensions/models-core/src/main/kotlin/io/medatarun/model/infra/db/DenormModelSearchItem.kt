package io.medatarun.model.infra.db

data class DenormModelSearchItem(
    val id: String,
    val itemType: String,
    val modelId: String,
    val modelKey: String,
    val modelLabel: String,
    val entityId: String?,
    val entityKey: String?,
    val entityLabel: String?,
    val relationshipId: String?,
    val relationshipKey: String?,
    val relationshipLabel: String?,
    val attributeId: String?,
    val attributeKey: String?,
    val attributeLabel: String?,
    val searchText: String
)