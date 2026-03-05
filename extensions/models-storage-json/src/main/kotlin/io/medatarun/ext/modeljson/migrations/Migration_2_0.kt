package io.medatarun.ext.modeljson.migrations

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.internal.ModelJsonRepositoryException
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonFiles
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.tags.core.domain.*
import io.medatarun.type.commons.key.KeyStrictValidation
import kotlinx.serialization.json.*

@Suppress("ClassName")
internal class Migration_2_0(
    private val files: ModelsStorageJsonFiles,
    private val json: Json,
    private val tagCmds: TagCmds,
    private val tagQueries: TagQueries,
    private val modelCmds: ModelCmds
) {

    /**
     * Reads all legacy hashtag values, rewrites the JSON storage to schema 2.0 with empty tag lists,
     * then immediately replays the collected tags through the official tag and model commands.
     */
    fun start(key: ModelKey, jsonObject: JsonObject) {
        val collector = ArrayList<PendingTagMigration>()
        val migratedRoot = migrateRootObject(key, jsonObject, collector)
        val migrated = LinkedHashMap<String, JsonElement>()

        for (entry in migratedRoot.entries) {
            if (entry.key == $$"$schema") {
                migrated[$$"$schema"] = JsonPrimitive(ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0))
                continue
            }
            migrated[entry.key] = entry.value
        }

        val updatedJson = JsonObject(migrated)
        val jsonString = json.encodeToString(JsonObject.serializer(), updatedJson)
        files.save(key.value, jsonString)
        replayPendingTagMigrations(collector)
    }

    private fun migrateRootObject(
        modelKey: ModelKey,
        jsonObject: JsonObject,
        collector: MutableList<PendingTagMigration>
    ): JsonObject {
        val modelMigrationRef = PendingTagMigrationRef.Model
        collectLegacyTags(modelKey, modelMigrationRef, jsonObject, collector)
        return migrateObject(modelKey, modelMigrationRef, jsonObject, collector)
    }

    private fun migrateObject(
        modelKey: ModelKey,
        migrationRef: PendingTagMigrationRef,
        jsonObject: JsonObject,
        collector: MutableList<PendingTagMigration>
    ): JsonObject {
        val migrated = LinkedHashMap<String, JsonElement>()

        for (entry in jsonObject.entries) {
            val entryKey = entry.key
            val entryValue = entry.value

            if (entryKey == "hashtags") {
                continue
            }

            if (entryKey == "entities") {
                migrated["entities"] = migrateEntities(modelKey, entryValue, collector)
                continue
            }

            if (entryKey == "relationships") {
                migrated["relationships"] = migrateRelationships(modelKey, entryValue, collector)
                continue
            }

            if (entryKey == "attributes") {
                migrated["attributes"] = migrateAttributes(modelKey, migrationRef, entryValue, collector)
                continue
            }

            if (entryKey == "tags") {
                migrated["tags"] = JsonArray(emptyList())
                continue
            }

            migrated[entryKey] = entryValue
        }

        if (!migrated.containsKey("tags")) {
            migrated["tags"] = JsonArray(emptyList())
        }

        return JsonObject(migrated)
    }

    private fun migrateEntities(
        modelKey: ModelKey,
        element: JsonElement,
        collector: MutableList<PendingTagMigration>
    ): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item !is JsonObject) {
                migrated.add(item)
                continue
            }

            val entityKey = item["key"]?.jsonPrimitive?.content
                ?: throw ModelJsonRepositoryException("Entity in ${modelKey.value}.json has no key during 2.0 migration.")
            val entityRef = PendingTagMigrationRef.Entity(EntityKey(entityKey))
            collectLegacyTags(modelKey, entityRef, item, collector)
            migrated.add(migrateObject(modelKey, entityRef, item, collector))
        }
        return JsonArray(migrated)
    }

    private fun migrateRelationships(
        modelKey: ModelKey,
        element: JsonElement,
        collector: MutableList<PendingTagMigration>
    ): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item !is JsonObject) {
                migrated.add(item)
                continue
            }

            val relationshipKey = item["key"]?.jsonPrimitive?.content
                ?: throw ModelJsonRepositoryException("Relationship in ${modelKey.value}.json has no key during 2.0 migration.")
            val relationshipRef = PendingTagMigrationRef.Relationship(RelationshipKey(relationshipKey))
            collectLegacyTags(modelKey, relationshipRef, item, collector)
            migrated.add(migrateObject(modelKey, relationshipRef, item, collector))
        }
        return JsonArray(migrated)
    }

    private fun migrateAttributes(
        modelKey: ModelKey,
        parentRef: PendingTagMigrationRef,
        element: JsonElement,
        collector: MutableList<PendingTagMigration>
    ): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item !is JsonObject) {
                migrated.add(item)
                continue
            }

            val attributeKey = item["key"]?.jsonPrimitive?.content
                ?: throw ModelJsonRepositoryException("Attribute in ${modelKey.value}.json has no key during 2.0 migration.")
            val attributeRef = when (parentRef) {
                is PendingTagMigrationRef.Entity -> {
                    PendingTagMigrationRef.EntityAttribute(parentRef.entityKey, AttributeKey(attributeKey))
                }

                is PendingTagMigrationRef.Relationship -> {
                    PendingTagMigrationRef.RelationshipAttribute(parentRef.relationshipKey, AttributeKey(attributeKey))
                }

                else -> {
                    throw ModelJsonRepositoryException(
                        "Attributes in ${modelKey.value}.json are not attached to an entity or relationship during 2.0 migration."
                    )
                }
            }
            collectLegacyTags(modelKey, attributeRef, item, collector)
            migrated.add(migrateObject(modelKey, attributeRef, item, collector))
        }
        return JsonArray(migrated)
    }

    private fun collectLegacyTags(
        modelKey: ModelKey,
        migrationRef: PendingTagMigrationRef,
        jsonObject: JsonObject,
        collector: MutableList<PendingTagMigration>
    ) {
        val elements = ArrayList<JsonArray>()
        val hashtags = jsonObject["hashtags"]
        if (hashtags is JsonArray) {
            elements.add(hashtags)
        }
        val tags = jsonObject["tags"]
        if (tags is JsonArray && !tags.all { it.isTagIdJsonPrimitive() }) {
            elements.add(tags)
        }

        for (element in elements) {
            for (item in element) {
                val rawTag = item.jsonPrimitiveOrNull() ?: continue
                val normalizedTag = normalizeLegacyTag(rawTag) ?: continue
                collector.add(PendingTagMigration(modelKey, migrationRef, normalizedTag))
            }
        }
    }

    private fun normalizeLegacyTag(rawValue: String): NormalizedLegacyTag? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return null
        }

        val separatorIndex = trimmed.indexOf(':')
        if (separatorIndex >= 0) {
            val groupPart = trimmed.substring(0, separatorIndex)
            val tagPart = trimmed.substring(separatorIndex + 1)
            val groupKey = normalizeTagGroupKey(groupPart) ?: return null
            val tagKey = normalizeTagKey(tagPart) ?: return null
            return NormalizedLegacyTag.Managed(groupKey, tagKey)
        }

        val tagKey = normalizeTagKey(trimmed) ?: return null
        return NormalizedLegacyTag.Free(tagKey)
    }

    private fun normalizeTagGroupKey(value: String): TagGroupKey? {
        val normalized = normalizeKeyValue(value) ?: return null
        return TagGroupKey(normalized)
    }

    private fun normalizeTagKey(value: String): TagKey? {
        val normalized = normalizeKeyValue(value) ?: return null
        return TagKey(normalized)
    }

    private fun normalizeKeyValue(value: String): String? {
        val filtered = buildString(value.length) {
            for (char in value.trim()) {
                if (isAllowedKeyChar(char)) {
                    append(char)
                }
            }
        }.take(KeyStrictValidation.MAX_LENGTH)

        if (filtered.isEmpty()) {
            return null
        }

        KeyStrictValidation.validate(filtered)
        return filtered
    }

    private fun isAllowedKeyChar(char: Char): Boolean {
        return char in 'a'..'z' ||
            char in 'A'..'Z' ||
            char in '0'..'9' ||
            char == '_' ||
            char == '-'
    }

    private fun JsonElement.jsonPrimitiveOrNull(): String? {
        return if (this is JsonPrimitive && isString) {
            content
        } else {
            null
        }
    }

    private fun JsonElement.isTagIdJsonPrimitive(): Boolean {
        val content = jsonPrimitiveOrNull() ?: return false
        return TAG_ID_REGEX.matches(content)
    }

    /**
     * Replays normalized legacy tags once the migrated file has been persisted in its 2.0 shape.
     * At that point the model storage is readable by the regular business services.
     */
    private fun replayPendingTagMigrations(pendingTagMigrations: List<PendingTagMigration>) {
        val dispatched = LinkedHashSet<String>()

        for (pending in pendingTagMigrations) {
            val tagRef = resolveTagRef(pending) ?: continue
            val dispatchKey = "${pending.modelKey.value}|${pending.target}|${tagRef.asString()}"
            if (!dispatched.add(dispatchKey)) {
                continue
            }

            val modelRef = ModelRef.ByKey(pending.modelKey)
            val cmd = when (val target = pending.target) {
                is PendingTagMigrationRef.Model -> {
                    ModelCmd.UpdateModelTagAdd(modelRef, tagRef)
                }

                is PendingTagMigrationRef.Entity -> {
                    ModelCmd.UpdateEntityTagAdd(modelRef, EntityRef.ByKey(target.entityKey), tagRef)
                }

                is PendingTagMigrationRef.EntityAttribute -> {
                    ModelCmd.UpdateEntityAttributeTagAdd(
                        modelRef,
                        EntityRef.ByKey(target.entityKey),
                        EntityAttributeRef.ByKey(target.attributeKey),
                        tagRef
                    )
                }

                is PendingTagMigrationRef.Relationship -> {
                    ModelCmd.UpdateRelationshipTagAdd(modelRef, RelationshipRef.ByKey(target.relationshipKey), tagRef)
                }

                is PendingTagMigrationRef.RelationshipAttribute -> {
                    ModelCmd.UpdateRelationshipAttributeTagAdd(
                        modelRef,
                        RelationshipRef.ByKey(target.relationshipKey),
                        RelationshipAttributeRef.ByKey(target.attributeKey),
                        tagRef
                    )
                }
            }
            modelCmds.dispatch(cmd)
        }
    }

    private fun resolveTagRef(pending: PendingTagMigration): TagRef? {
        return when (val tag = pending.tag) {
            is NormalizedLegacyTag.Managed -> {
                val existingGroup = tagQueries.findTagGroupByKeyOptional(tag.groupKey)
                val group = if (existingGroup != null) {
                    existingGroup
                } else {
                    tagCmds.dispatch(TagCmd.TagGroupCreate(tag.groupKey, null, null))
                    tagQueries.findTagGroupByKeyOptional(tag.groupKey) ?: return null
                }

                val tagRef = TagRef.ByKey(TagScopeRef.Global, group.key, tag.tagKey)
                if (tagQueries.findTagByRefOptional(tagRef) == null) {
                    tagCmds.dispatch(TagCmd.TagManagedCreate(TagGroupRef.ById(group.id), tag.tagKey, null, null))
                }
                tagRef
            }

            is NormalizedLegacyTag.Free -> {
                val modelId = readModelId(pending.modelKey) ?: return null
                val scopeRef = modelTagScopeRef(modelId)
                val tagRef = TagRef.ByKey(scopeRef, null, tag.tagKey)
                if (tagQueries.findTagByRefOptional(tagRef) == null) {
                    tagCmds.dispatch(TagCmd.TagFreeCreate(scopeRef, tag.tagKey, null, null))
                }
                tagRef
            }
        }
    }

    private fun readModelId(key: ModelKey): ModelId? {
        return Json.parseToJsonElement(files.load(key)).jsonObject["id"]?.jsonPrimitive?.content?.let { ModelId.fromString(it) }
    }

    companion object {
        private val TAG_ID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    }
}

internal data class PendingTagMigration(
    val modelKey: ModelKey,
    val target: PendingTagMigrationRef,
    val tag: NormalizedLegacyTag
)

internal sealed interface PendingTagMigrationRef {
    data object Model : PendingTagMigrationRef
    data class Entity(val entityKey: EntityKey) : PendingTagMigrationRef
    data class EntityAttribute(val entityKey: EntityKey, val attributeKey: AttributeKey) : PendingTagMigrationRef
    data class Relationship(val relationshipKey: RelationshipKey) : PendingTagMigrationRef
    data class RelationshipAttribute(val relationshipKey: RelationshipKey, val attributeKey: AttributeKey) : PendingTagMigrationRef
}

internal sealed interface NormalizedLegacyTag {
    data class Managed(val groupKey: TagGroupKey, val tagKey: TagKey) : NormalizedLegacyTag
    data class Free(val tagKey: TagKey) : NormalizedLegacyTag
}
