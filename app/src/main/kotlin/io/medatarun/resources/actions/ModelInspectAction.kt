package io.medatarun.resources.actions

import io.medatarun.runtime.AppRuntime

class ModelInspectAction(val runtime: AppRuntime) {
    fun process(): String {
        val buf = StringBuilder()
        val modelId = runtime.modelQueries.findAllModelIds()
        modelId.forEach { modelId ->
            val model = runtime.modelQueries.findModelById(modelId)
            buf.appendLine("🌐 ${model.id.value}")
            model.entityDefs.forEach { entity ->
                buf.appendLine("  📦 ${entity.id.value}")
                entity.attributes.forEach { attribute ->
                    val name = attribute.id.value
                    val type = attribute.type.value
                    val optional = if (attribute.optional) "?" else ""
                    val pk = if (entity.identifierAttributeDefId == attribute.id) "🔑" else ""
                    buf.appendLine("    $name: $type$optional $pk")
                }
            }
        }
        return buf.toString()
    }
}