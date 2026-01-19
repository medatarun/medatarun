package io.medatarun.ext.modeljson

import io.medatarun.model.domain.Model
import io.medatarun.model.ports.needs.ModelExporter
import kotlinx.serialization.json.JsonObject

class ModelExporterJson: ModelExporter {
    override fun exportJson(model: Model): JsonObject {
        return ModelJsonConverter(true).toJsonObject(model)
    }

}
