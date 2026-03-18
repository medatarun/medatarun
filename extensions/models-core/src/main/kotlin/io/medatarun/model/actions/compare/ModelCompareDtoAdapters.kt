package io.medatarun.model.actions.compare

import io.medatarun.model.domain.EntityOrigin
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelOrigin
import io.medatarun.model.domain.diff.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object ModelCompareDtoAdapters {

    fun toCompareDto(diff: ModelDiff): ModelCompareDto {
        val entries = diff.entries.map { entry -> toCompareEntryDto(entry) }
        return ModelCompareDto(
            scopeApplied = diff.scopeApplied.code,
            left = ModelCompareSideDto(
                modelId = diff.left.modelId.asString(),
                modelKey = diff.left.modelKey.value,
                modelVersion = diff.left.modelVersion.value,
                modelAuthority = diff.left.modelAuthority.code
            ),
            right = ModelCompareSideDto(
                modelId = diff.right.modelId.asString(),
                modelKey = diff.right.modelKey.value,
                modelVersion = diff.right.modelVersion.value,
                modelAuthority = diff.right.modelAuthority.code
            ),
            entries = entries
        )
    }
}

@Serializable
data class ModelCompareDto(
    val scopeApplied: String,
    val left: ModelCompareSideDto,
    val right: ModelCompareSideDto,
    val entries: List<ModelCompareEntryDto>
)

@Serializable
data class ModelCompareSideDto(
    val modelId: String,
    val modelKey: String,
    val modelVersion: String,
    val modelAuthority: String
)

@Serializable
data class ModelCompareEntryDto(
    val status: String,
    val objectType: String,
    val modelKey: String,
    val typeKey: String?,
    val entityKey: String?,
    val relationshipKey: String?,
    val roleKey: String?,
    val attributeKey: String?,
    val left: JsonObject?,
    val right: JsonObject?
)

private fun toCompareEntryDto(entry: ModelDiffEntry): ModelCompareEntryDto {
    val locationData = toLocationData(entry.location)
    return when (entry) {
        is ModelDiffEntry.Added -> ModelCompareEntryDto(
            status = "ADDED",
            objectType = entry.location.objectType,
            modelKey = locationData.modelKey,
            typeKey = locationData.typeKey,
            entityKey = locationData.entityKey,
            relationshipKey = locationData.relationshipKey,
            roleKey = locationData.roleKey,
            attributeKey = locationData.attributeKey,
            left = null,
            right = toSnapshotJson(entry.right)
        )

        is ModelDiffEntry.Deleted -> ModelCompareEntryDto(
            status = "DELETED",
            objectType = entry.location.objectType,
            modelKey = locationData.modelKey,
            typeKey = locationData.typeKey,
            entityKey = locationData.entityKey,
            relationshipKey = locationData.relationshipKey,
            roleKey = locationData.roleKey,
            attributeKey = locationData.attributeKey,
            left = toSnapshotJson(entry.left),
            right = null
        )

        is ModelDiffEntry.Modified -> ModelCompareEntryDto(
            status = "MODIFIED",
            objectType = entry.location.objectType,
            modelKey = locationData.modelKey,
            typeKey = locationData.typeKey,
            entityKey = locationData.entityKey,
            relationshipKey = locationData.relationshipKey,
            roleKey = locationData.roleKey,
            attributeKey = locationData.attributeKey,
            left = toSnapshotJson(entry.left),
            right = toSnapshotJson(entry.right)
        )
    }
}

private data class CompareLocationData(
    val modelKey: String,
    val typeKey: String?,
    val entityKey: String?,
    val relationshipKey: String?,
    val roleKey: String?,
    val attributeKey: String?
)

private fun toLocationData(location: ModelDiffLocation): CompareLocationData {
    return when (location) {
        is ModelDiffModelLocation -> CompareLocationData(
            modelKey = location.modelKey.value,
            typeKey = null,
            entityKey = null,
            relationshipKey = null,
            roleKey = null,
            attributeKey = null
        )

        is ModelDiffTypeLocation -> CompareLocationData(
            modelKey = location.modelKey.value,
            typeKey = location.typeKey.value,
            entityKey = null,
            relationshipKey = null,
            roleKey = null,
            attributeKey = null
        )

        is ModelDiffEntityLocation -> CompareLocationData(
            modelKey = location.modelKey.value,
            typeKey = null,
            entityKey = location.entityKey.value,
            relationshipKey = null,
            roleKey = null,
            attributeKey = null
        )

        is ModelDiffEntityAttributeLocation -> CompareLocationData(
            modelKey = location.modelKey.value,
            typeKey = null,
            entityKey = location.entityKey.value,
            relationshipKey = null,
            roleKey = null,
            attributeKey = location.attributeKey.value
        )

        is ModelDiffRelationshipLocation -> CompareLocationData(
            modelKey = location.modelKey.value,
            typeKey = null,
            entityKey = null,
            relationshipKey = location.relationshipKey.value,
            roleKey = null,
            attributeKey = null
        )

        is ModelDiffRelationshipRoleLocation -> CompareLocationData(
            modelKey = location.modelKey.value,
            typeKey = null,
            entityKey = null,
            relationshipKey = location.relationshipKey.value,
            roleKey = location.roleKey.value,
            attributeKey = null
        )

        is ModelDiffRelationshipAttributeLocation -> CompareLocationData(
            modelKey = location.modelKey.value,
            typeKey = null,
            entityKey = null,
            relationshipKey = location.relationshipKey.value,
            roleKey = null,
            attributeKey = location.attributeKey.value
        )
    }
}

private fun toSnapshotJson(snapshot: ModelDiffSnapshot): JsonObject {
    return when (snapshot) {
        is ModelDiffModelSnapshot -> buildJsonObject {
            put("objectType", snapshot.objectType)
            put("key", snapshot.key.value)
            putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
            putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
            put("version", snapshot.version.value)
            put("origin", toModelOriginCode(snapshot.origin))
            put("authority", snapshot.authority.code)
            put("documentationHome", snapshot.documentationHome?.toExternalForm())
            putJsonArray("tags") {
                snapshot.tags.forEach { tag -> add(tag.value.toString()) }
            }
        }

        is ModelDiffTypeSnapshot -> buildJsonObject {
            put("objectType", snapshot.objectType)
            put("key", snapshot.key.value)
            putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
            putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
        }

        is ModelDiffEntitySnapshot -> buildJsonObject {
            put("objectType", snapshot.objectType)
            put("key", snapshot.key.value)
            putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
            putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
            put("identifierAttributeKey", snapshot.identifierAttributeKey.value)
            put("origin", toEntityOriginCode(snapshot.origin))
            put("documentationHome", snapshot.documentationHome?.toExternalForm())
            putJsonArray("tags") {
                snapshot.tags.forEach { tag -> add(tag.value.toString()) }
            }
        }

        is ModelDiffEntityAttributeSnapshot -> buildJsonObject {
            put("objectType", snapshot.objectType)
            put("key", snapshot.key.value)
            putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
            putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
            put("typeKey", snapshot.typeKey.value)
            put("optional", snapshot.optional)
            putJsonArray("tags") {
                snapshot.tags.forEach { tag -> add(tag.value.toString()) }
            }
        }

        is ModelDiffRelationshipSnapshot -> buildJsonObject {
            put("objectType", snapshot.objectType)
            put("key", snapshot.key.value)
            putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
            putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
            putJsonArray("tags") {
                snapshot.tags.forEach { tag -> add(tag.value.toString()) }
            }
        }

        is ModelDiffRelationshipRoleSnapshot -> buildJsonObject {
            put("objectType", snapshot.objectType)
            put("key", snapshot.key.value)
            put("entityKey", snapshot.entityKey.value)
            putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
            put("cardinality", snapshot.cardinality.code)
        }

        is ModelDiffRelationshipAttributeSnapshot -> buildJsonObject {
            put("objectType", snapshot.objectType)
            put("key", snapshot.key.value)
            putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
            putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
            put("typeKey", snapshot.typeKey.value)
            put("optional", snapshot.optional)
            putJsonArray("tags") {
                snapshot.tags.forEach { tag -> add(tag.value.toString()) }
            }
        }
    }
}

private fun putNullableJsonField(
    builder: JsonObjectBuilder,
    key: String,
    value: JsonElement?
) {
    if (value == null) {
        builder.put(key, JsonNull)
        return
    }
    builder.put(key, value)
}

private fun toLocalizedTextJson(value: LocalizedText?): JsonElement? {
    if (value == null) return null
    if (!value.isLocalized) return JsonPrimitive(value.name)
    val values = value.all()
    return buildJsonObject {
        values.entries.forEach { item ->
            put(item.key, item.value)
        }
    }
}

private fun toLocalizedMarkdownJson(value: LocalizedMarkdown?): JsonElement? {
    if (value == null) return null
    if (!value.isLocalized) return JsonPrimitive(value.name)
    val values = value.all()
    return buildJsonObject {
        values.entries.forEach { item ->
            put(item.key, item.value)
        }
    }
}

private fun toModelOriginCode(origin: ModelOrigin): String {
    return when (origin) {
        is ModelOrigin.Manual -> "manual"
        is ModelOrigin.Uri -> origin.uri.toString()
    }
}

private fun toEntityOriginCode(origin: EntityOrigin): String {
    return when (origin) {
        is EntityOrigin.Manual -> "manual"
        is EntityOrigin.Uri -> origin.uri.toString()
    }
}