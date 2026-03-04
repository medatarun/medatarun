package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.ModelAggregate

interface ModelHumanPrinter {
    fun print(model: ModelAggregate): String
}