package io.medatarun.model.ports.needs

import io.medatarun.model.domain.ModelAggregate
import io.medatarun.platform.kernel.ServiceContributionPoint
import kotlinx.serialization.json.JsonObject

interface ModelExporter: ServiceContributionPoint {
    fun exportJson(model: ModelAggregate): JsonObject
}