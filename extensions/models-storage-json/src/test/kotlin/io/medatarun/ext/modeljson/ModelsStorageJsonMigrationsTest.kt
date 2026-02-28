package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.internal.ModelsStorageJsonFiles
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonMigrations
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ModelsStorageJsonMigrationsTest {

    private val parser = Json { prettyPrint = true }

    @Test
    fun `migrates schema from 1_0 to 2_0 through 1_1`() {
        val fs = ModelJsonFilesystemFixture()
        val files = ModelsStorageJsonFiles(fs.modelsDirectory())
        files.save("legacy-model", createSchema_1_0_ModelJson().toString())

        ModelsStorageJsonMigrations(files, prettyPrint = true).start()

        val migrated = parser.parseToJsonElement(files.load(io.medatarun.model.domain.ModelKey("legacy-model"))).jsonObject
        assertEquals(
            ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0),
            migrated[$$"$schema"]?.jsonPrimitive?.content
        )
        assertNotNull(migrated["id"]?.jsonPrimitive?.content)
        assertEquals("legacy-model", migrated["key"]?.jsonPrimitive?.content)
    }

    @Test
    fun `migrates schema from 1_1 to 2_0`() {
        val fs = ModelJsonFilesystemFixture()
        val files = ModelsStorageJsonFiles(fs.modelsDirectory())
        files.save("current-model", createSchema_1_1_ModelJson().toString())

        ModelsStorageJsonMigrations(files, prettyPrint = true).start()

        val migrated = parser.parseToJsonElement(files.load(io.medatarun.model.domain.ModelKey("current-model"))).jsonObject
        assertEquals(
            ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0),
            migrated[$$"$schema"]?.jsonPrimitive?.content
        )
        assertEquals("model-id", migrated["id"]?.jsonPrimitive?.content)
        assertEquals("current-model", migrated["key"]?.jsonPrimitive?.content)
    }

    private fun createSchema_1_0_ModelJson() = buildJsonObject {
        put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_1_0))
        put("id", "legacy-model")
        put("version", "1.0.0")
        put("hashtags", buildJsonArray { add(JsonPrimitive("tag:legacy")) })
        putJsonArray("types") {
            add(buildJsonObject {
                put("id", "string")
            })
        }
        putJsonArray("entities") {
            add(buildJsonObject {
                put("id", "customer")
                put("identifierAttribute", "id")
                put("hashtags", buildJsonArray { add(JsonPrimitive("tag:entity")) })
                putJsonArray("attributes") {
                    add(buildJsonObject {
                        put("id", "id")
                        put("type", "string")
                    })
                }
            })
        }
    }

    private fun createSchema_1_1_ModelJson() = buildJsonObject {
        put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_1_1))
        put("id", "model-id")
        put("key", "current-model")
        put("version", "1.0.0")
        put("tags", buildJsonArray { add(JsonPrimitive("tag:current")) })
        putJsonArray("types") {
            add(buildJsonObject {
                put("id", "type-id")
                put("key", "string")
            })
        }
        putJsonArray("entities") {
            add(buildJsonObject {
                put("id", "entity-id")
                put("key", "customer")
                put("identifierAttribute", "id")
                put("tags", buildJsonArray { add(JsonPrimitive("tag:entity")) })
                putJsonArray("attributes") {
                    add(buildJsonObject {
                        put("id", "attribute-id")
                        put("key", "id")
                        put("type", "string")
                    })
                }
            })
        }
    }
}
