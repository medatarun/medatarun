package io.medatarun.ext.modeljson.internal

import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.ports.needs.ModelExporter
import kotlinx.serialization.json.JsonObject

internal class ModelExporterJson(val converter: ModelJsonConverter): ModelExporter {
    override fun exportJson(model: ModelAggregate): JsonObject {
        return converter.toJsonObject(model)
    }

}