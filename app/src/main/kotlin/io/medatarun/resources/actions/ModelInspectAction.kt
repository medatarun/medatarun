package io.medatarun.resources.actions

import io.medatarun.runtime.AppRuntime

class ModelInspectAction(val runtime: AppRuntime) {
    fun process(): String {

        val buf = StringBuilder()
        val modelId = runtime.modelQueries.findAllModelIds()
        modelId.forEach { modelId ->
            val model = runtime.modelQueries.findModelById(modelId)
            buf.append(runtime.modelHumanPrinter.print(model))

        }
        return buf.toString()
    }
}