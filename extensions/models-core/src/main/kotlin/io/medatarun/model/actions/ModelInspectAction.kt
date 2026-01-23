package io.medatarun.model.actions

import io.medatarun.model.ports.exposed.ModelHumanPrinter
import io.medatarun.model.ports.exposed.ModelQueries

class ModelInspectAction(val modelQueries: ModelQueries, val modelHumanPrinter: ModelHumanPrinter) {
    fun process(): String {

        val buf = StringBuilder()
        val modelId = modelQueries.findAllModelKeys()
        modelId.forEach { modelId ->
            val model = modelQueries.findModelByKey(modelId)
            buf.append(modelHumanPrinter.print(model))

        }
        return buf.toString()
    }
}