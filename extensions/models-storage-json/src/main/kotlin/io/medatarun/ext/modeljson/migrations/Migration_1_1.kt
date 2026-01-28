package io.medatarun.ext.modeljson.migrations

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.internal.ModelJsonRepositoryException
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonFiles
import io.medatarun.model.domain.*
import kotlinx.serialization.json.*

@Suppress("ClassName")
internal class Migration_1_1(private val files: ModelsStorageJsonFiles, private val json : Json) {
    fun start() {
        files.getAllModelFiles().forEach { f ->
            val jsonObject = Json.parseToJsonElement(files.load(f.key)).jsonObject

            val schema = jsonObject.getOrDefault($$"$schema", null)?.jsonPrimitive?.content
                ?: throw ModelJsonRepositoryException("Model without medatarun schema: ${f.value}. Storage corrupted. Stopping.")

            if (schema.endsWith(ModelJsonSchemas.v_1_0)) {
                Migration_1_1(files, json).start(f.key, jsonObject)
            }
        }
    }
    private fun start(key: ModelKey, jsonObject: JsonObject) {
        // Apply the schema 1.1 transformations directly on the provided JsonObject and persist it.
        val renamedRoot = renameIdToKey(jsonObject)
        val migrated = LinkedHashMap<String, JsonElement>()

        for (entry in renamedRoot.entries) {
            val entryKey = entry.key
            val entryValue = entry.value

            migrated["id"] = JsonPrimitive(ModelId.generate().value.toString())

            if (entryKey == $$"$schema") {
                migrated[$$"$schema"] = JsonPrimitive(ModelJsonSchemas.forVersion(ModelJsonSchemas.v_1_1))
                continue
            }

            if (entryKey == "types") {
                migrated["types"] = migrateTypeArray(entryValue)
                continue
            }

            if (entryKey == "entities") {
                migrated["entities"] = migrateEntityArray(entryValue)
                continue
            }

            if (entryKey == "relationships") {
                migrated["relationships"] = migrateRelationshipArray(entryValue)
                continue
            }
            migrated[entryKey] = entryValue
        }

        val updatedJson = JsonObject(migrated)
        val jsonString = json.encodeToString(JsonObject.serializer(), updatedJson)
        files.save(key.value, jsonString)
    }

    private fun migrateTypeArray(element: JsonElement): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item is JsonObject) {
                migrated.add(renameIdThenAddId(item, TypeId.generate().value.toString()))
            } else {
                migrated.add(item)
            }
        }
        return JsonArray(migrated)
    }

    private fun migrateEntityArray(element: JsonElement): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item is JsonObject) {
                val renamed = renameIdToKey(item)
                val entityObject = LinkedHashMap<String, JsonElement>()
                entityObject["id"] = JsonPrimitive(EntityId.generate().value.toString())
                for (entry in renamed.entries) {
                    val entryKey = entry.key
                    val entryValue = entry.value
                    if (entryKey == "attributes") {
                        entityObject["attributes"] = migrateAttributeArray(entryValue)
                        continue
                    }
                    entityObject[entryKey] = entryValue
                }
                migrated.add(JsonObject(entityObject))
            } else {
                migrated.add(item)
            }
        }
        return JsonArray(migrated)
    }

    private fun migrateRelationshipArray(element: JsonElement): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item is JsonObject) {
                val renamed = renameIdToKey(item)
                val relObject = LinkedHashMap<String, JsonElement>()
                relObject["id"] = JsonPrimitive(RelationshipId.generate().value.toString())
                for (entry in renamed.entries) {
                    val entryKey = entry.key
                    val entryValue = entry.value
                    if (entryKey == "attributes") {
                        relObject["attributes"] = migrateAttributeArray(entryValue)
                        continue
                    }
                    if (entryKey == "roles") {
                        relObject["roles"] = migrateRoleArray(entryValue)
                        continue
                    }
                    relObject[entryKey] = entryValue
                }
                migrated.add(JsonObject(relObject))
            } else {
                migrated.add(item)
            }
        }
        return JsonArray(migrated)
    }

    private fun migrateAttributeArray(element: JsonElement): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item is JsonObject) {
                migrated.add(renameIdThenAddId(item, AttributeId.generate().value.toString()))
            } else {
                migrated.add(item)
            }
        }
        return JsonArray(migrated)
    }

    private fun migrateRoleArray(element: JsonElement): JsonArray {
        if (element !is JsonArray) {
            return JsonArray(emptyList())
        }

        val migrated = ArrayList<JsonElement>(element.size)
        for (item in element) {
            if (item is JsonObject) {
                migrated.add(renameIdThenAddId(item, RelationshipRoleId.generate().value.toString()))
            } else {
                migrated.add(item)
            }
        }
        return JsonArray(migrated)
    }

    private fun renameIdToKey(original: JsonObject): JsonObject {
        // Renames "id" to "key" while preserving order for all other fields.
        val updated = LinkedHashMap<String, JsonElement>()
        for (entry in original.entries) {
            val entryKey = entry.key
            val entryValue = entry.value
            if (entryKey == "id") {
                updated["key"] = entryValue
            } else {
                updated[entryKey] = entryValue
            }
        }
        return JsonObject(updated)
    }

    private fun renameIdThenAddId(original: JsonObject, newId: String): JsonObject {
        // First rename "id" to "key", then insert "id" as first field.
        val renamed = renameIdToKey(original)
        val updated = LinkedHashMap<String, JsonElement>()
        updated["id"] = JsonPrimitive(newId)
        for (entry in renamed.entries) {
            updated[entry.key] = entry.value
        }
        return JsonObject(updated)
    }
}