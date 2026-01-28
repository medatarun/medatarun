package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeRef

data class AttributeIdentityInitializer(
    val attributeKey: AttributeKey,
    val type: TypeRef,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) {
    companion object {
        fun build(attributeKey: AttributeKey, type: TypeRef, block: Builder.() -> Unit = {}):AttributeIdentityInitializer {
            return Builder(attributeKey, type).apply(block).build()
        }
        class Builder(var  attributeKey: AttributeKey, var type: TypeRef) {
            var name: LocalizedText? = null
            var description: LocalizedMarkdown? = null
            fun build(): AttributeIdentityInitializer {
                return AttributeIdentityInitializer(attributeKey, type, name, description)
            }
        }
    }
}