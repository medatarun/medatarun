package io.medatarun.model.ports.needs

import io.medatarun.model.domain.ModelAggregate
import kotlinx.serialization.json.JsonObject

interface ModelExporter {
    fun exportJson(model: ModelAggregate): JsonObject
}