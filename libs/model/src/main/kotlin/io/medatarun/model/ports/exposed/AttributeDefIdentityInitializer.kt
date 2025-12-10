package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelTypeId

data class AttributeDefIdentityInitializer(
    val attributeDefId: AttributeDefId,
    val type: ModelTypeId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) {
    companion object {
        fun build(attributeDefId: AttributeDefId, type: ModelTypeId, block: Builder.() -> Unit = {}):AttributeDefIdentityInitializer {
            return Builder(attributeDefId, type).apply(block).build()
        }
        class Builder(var  attributeDefId: AttributeDefId, var type: ModelTypeId) {
            var name: LocalizedText? = null
            var description: LocalizedMarkdown? = null
            fun build(): AttributeDefIdentityInitializer {
                return AttributeDefIdentityInitializer(attributeDefId, type, name, description)
            }
        }
    }
}