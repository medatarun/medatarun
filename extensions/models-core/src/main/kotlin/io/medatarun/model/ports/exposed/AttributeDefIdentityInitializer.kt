package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeRef

data class AttributeDefIdentityInitializer(
    val attributeKey: AttributeKey,
    val type: TypeRef,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) {
    companion object {
        fun build(attributeKey: AttributeKey, type: TypeRef, block: Builder.() -> Unit = {}):AttributeDefIdentityInitializer {
            return Builder(attributeKey, type).apply(block).build()
        }
        class Builder(var  attributeKey: AttributeKey, var type: TypeRef) {
            var name: LocalizedText? = null
            var description: LocalizedMarkdown? = null
            fun build(): AttributeDefIdentityInitializer {
                return AttributeDefIdentityInitializer(attributeKey, type, name, description)
            }
        }
    }
}