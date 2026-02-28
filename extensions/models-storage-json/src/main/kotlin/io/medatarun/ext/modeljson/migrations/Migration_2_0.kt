package io.medatarun.ext.modeljson.migrations

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonFiles
import io.medatarun.model.domain.ModelKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Suppress("ClassName")
internal class Migration_2_0(private val files: ModelsStorageJsonFiles, private val json: Json) {

    /**
     * For now the 2.0 migration only updates the declared schema version.
     * Later steps will replace the legacy JSON tag shape with structured tag refs.
     */
    fun start(key: ModelKey, jsonObject: JsonObject) {
        val migrated = LinkedHashMap<String, kotlinx.serialization.json.JsonElement>()

        for (entry in jsonObject.entries) {
            if (entry.key == $$"$schema") {
                migrated[$$"$schema"] = JsonPrimitive(ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0))
                continue
            }
            migrated[entry.key] = entry.value
        }

        val updatedJson = JsonObject(migrated)
        val jsonString = json.encodeToString(JsonObject.serializer(), updatedJson)
        files.save(key.value, jsonString)
    }
}
