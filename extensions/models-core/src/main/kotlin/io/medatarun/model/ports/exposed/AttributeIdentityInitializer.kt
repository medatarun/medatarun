package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine
import io.medatarun.model.domain.TypeRef

data class AttributeIdentityInitializer(
    val attributeKey: AttributeKey,
    val type: TypeRef,
    val name: TextSingleLine?,
    val description: TextMarkdown?
) {
    companion object {
        fun build(
            attributeKey: AttributeKey,
            type: TypeRef,
            block: Builder.() -> Unit = {}
        ): AttributeIdentityInitializer {
            return Builder(attributeKey, type).apply(block).build()
        }

        class Builder(var attributeKey: AttributeKey, var type: TypeRef) {
            var name: TextSingleLine? = null
            var description: TextMarkdown? = null
            fun build(): AttributeIdentityInitializer {
                return AttributeIdentityInitializer(attributeKey, type, name, description)
            }
        }
    }
}