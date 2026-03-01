package io.medatarun.ext.modeljson.internal

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.migrations.Migration_1_1
import io.medatarun.ext.modeljson.migrations.Migration_2_0
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.domain.ModelKey
import io.medatarun.platform.kernel.PlatformStartedCtx
import io.medatarun.platform.kernel.PlatformStartedListener
import io.medatarun.platform.kernel.getService
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class ModelsStorageJsonMigrations(private val files: ModelsStorageJsonFiles, val prettyPrint: Boolean) :
    PlatformStartedListener {

    private val json = Json { prettyPrint = this@ModelsStorageJsonMigrations.prettyPrint }

    override fun onPlatformStarted(ctx: PlatformStartedCtx) {
        start(
            tagCmds = ctx.services.getService<TagCmds>(),
            tagQueries = ctx.services.getService<TagQueries>(),
            modelCmds = ctx.services.getService<ModelCmds>()
        )
    }

    internal fun start(tagCmds: TagCmds, tagQueries: TagQueries, modelCmds: ModelCmds) {
        val migration_1_1 = Migration_1_1(files, json)
        val migration_2_0 = Migration_2_0(files, json, tagCmds, tagQueries, modelCmds)

        for (file in files.getAllModelFiles()) {
            migrateFileToCurrentVersion(file.key, migration_1_1, migration_2_0)
        }
    }

    /**
     * Applies each intermediate storage migration until the file reaches the current schema version.
     */
    private fun migrateFileToCurrentVersion(
        key: ModelKey,
        migration_1_1: Migration_1_1,
        migration_2_0: Migration_2_0
    ) {
        var schema = readSchema(key)

        while (schema != ModelJsonSchemas.current()) {
            when {
                schema.endsWith(ModelJsonSchemas.v_1_0) -> {
                    migration_1_1.start(key, loadJsonObject(key))
                }

                schema.endsWith(ModelJsonSchemas.v_1_1) -> {
                    migration_2_0.start(key, loadJsonObject(key))
                }

                else -> {
                    throw ModelJsonRepositoryException(
                        "Unsupported medatarun schema for ${key.value}.json: $schema. Storage corrupted. Stopping."
                    )
                }
            }

            schema = readSchema(key)
        }
    }

    private fun readSchema(key: ModelKey): String {
        val jsonObject = loadJsonObject(key)
        return jsonObject[$$"$schema"]?.let { it.toString().removePrefix("\"").removeSuffix("\"") }
            ?: throw ModelJsonRepositoryException("Model without medatarun schema: ${key.value}.json. Storage corrupted. Stopping.")
    }

    private fun loadJsonObject(key: ModelKey): JsonObject {
        return Json.parseToJsonElement(files.load(key)).jsonObject
    }
}
