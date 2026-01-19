package io.medatarun.model.ports.needs

import io.medatarun.model.domain.Model
import kotlinx.serialization.json.JsonObject

interface ModelExporter {
    fun exportJson(model: Model): JsonObject
}