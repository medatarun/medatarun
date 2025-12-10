package io.medatarun.model.infra

import io.medatarun.model.domain.Model
import io.medatarun.model.ports.exposed.ModelHumanPrinter

class ModelHumanPrinterEmoji: ModelHumanPrinter {

    fun tabs(number: Int): String = "   ".repeat(number)

    override fun print(model: Model): String {
        val buf = StringBuffer()
        buf.appendLine("🌐 ${model.id.value}")
        val modelName = model.name
        if (modelName != null) {
            buf.appendLine(tabs(1) + modelName.name)
        }
        val modelDescription = model.description
        if (modelDescription != null) {
            buf.appendLine(tabs(1) + modelDescription.name)
        }
        if (model.types.isNotEmpty()) {
            buf.appendLine(tabs(1) + "🎫 Types")
        }
        model.types.forEach { type ->
            buf.appendLine(tabs(2) + type.id.value)
        }
        model.entityDefs.forEach { entity ->
            buf.appendLine(tabs(1) + "📦 ${entity.id.value}")
            val entityName = entity.name?.name
            if (entityName != null) {
                buf.appendLine(tabs(2) + entityName)
            }
            val entityDescription = entity.description?.name
            if (entityDescription != null) {
                buf.appendLine(tabs(2) + entityDescription)
            }
            entity.attributes.forEach { attribute ->
                val id = attribute.id.value
                val type = attribute.type.value
                val optional = if (attribute.optional) "?" else ""
                val pk = if (entity.identifierAttributeKey == attribute.id) "🔑" else ""
                buf.appendLine(tabs(2) + "-  $id: $type$optional $pk")
                val name = attribute.name
                if (name != null) {
                    buf.appendLine(tabs(3) + name.name)
                }
                val description = attribute.description
                if (description != null) {
                    buf.appendLine(tabs(3) + description.name)
                }
            }
        }
        return buf.toString()
    }
}