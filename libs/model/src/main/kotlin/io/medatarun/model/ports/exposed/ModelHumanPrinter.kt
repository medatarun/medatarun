package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.Model

interface ModelHumanPrinter {
    fun print(model: Model): String
}