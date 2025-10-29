package io.medatarun.model.infra

import io.medatarun.model.model.ModelHumanPrinter
import io.medatarun.model.model.Model

class ModelHumanPrinterEmoji: ModelHumanPrinter {
    override fun print(model: Model): String {
        val buf = StringBuffer()
        buf.appendLine("🌐 ${model.id.value}")
        if (model.types.isNotEmpty()) {
            buf.appendLine("  🎫 Types")
        }
        model.types.forEach { type ->
            buf.appendLine("    "  + type.id.value)
        }
        model.entityDefs.forEach { entity ->
            buf.appendLine("  📦 ${entity.id.value}")
            entity.attributes.forEach { attribute ->
                val id = attribute.id.value
                val type = attribute.type.value
                val optional = if (attribute.optional) "?" else ""
                val pk = if (entity.identifierAttributeDefId == attribute.id) "🔑" else ""
                buf.appendLine("    $id: $type$optional $pk")
                val name = attribute.name
                if (name != null) {
                    buf.appendLine("      " + name.name)
                }
                val description = attribute.description
                if (description != null) {
                    buf.appendLine("      " + description.name)
                }
            }
        }
        return buf.toString()
    }
}