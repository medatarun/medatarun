package io.medatarun.ext.modeljson

import io.medatarun.model.domain.ModelKey
import io.medatarun.platform.kernel.PlatformStartedCtx
import io.medatarun.platform.kernel.PlatformStartedListener
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path

class ModelsStorageJsonMigrations(val files: ModelsJsonStorageFiles, val repo: ModelJsonRepository): PlatformStartedListener {
    override fun onPlatformStarted(ctx: PlatformStartedCtx) {

        // First migration, if model has no id, load and save model to have ids everywhere

        files.getAllModelFiles().forEach { f ->
            val jsonObject = Json.parseToJsonElement(files.load(f.key)).jsonObject

            val schema = jsonObject.getOrDefault($$"$schema", null)?.jsonPrimitive?.content
                ?: throw ModelJsonRepositoryException("Model without medatarun schema: ${f.value}. Storage corrupted. Stopping.")

            if (schema.endsWith(ModelJsonSchemas.v_1_0)) {
                migration_1_1(f.key, f.value, jsonObject)
            }
        }
    }

    private fun migration_1_1(key: ModelKey, value: Path, jsonObject: JsonObject) {
        // rename root attribute "id" to "key"
        // add "id" with ModelId.generated() to root after "$schema"
        // in "types" array, rename "id" to "key"
        // in "types" array, add "id" with TypeId.generated() in first position
        // in "entities" array, rename "id" to "key"
        // in "entities" array, add "id" with EntityId.generated() in first position
        // in "entities.attributes" array, rename "id" to "key"
        // in "entities.attributes" array, add "id" with AttributeId.generated() in first position
        // in "relationships" array, rename "id" to "key"
        // in "relationships" array, add "id" with RelationshipId.generated() in first position
        // in "relationships.attributes" array, rename "id" to "key"
        // in "relationships.attributes" array, add "id" with AttributeId.generated() in first position
        // in "relationships.roles" array, rename "id" to "key"
        // in "relationships.roles" array, add "id" with RelationshipRoleId.generated() in first position

    }

}
