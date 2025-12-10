package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import java.net.URL

data class EntityDefInitializer(
    val entityKey: EntityKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identityAttribute: AttributeDefIdentityInitializer,
    val documentationHome: URL?
) {
    companion object {
        fun build(entityKey: EntityKey, identityAttribute: AttributeDefIdentityInitializer, block: Builder.() -> Unit = {}): EntityDefInitializer {
            return Builder(entityKey, identityAttribute).apply(block).build()
        }
        class Builder(var entityKey: EntityKey, var identityAttribute: AttributeDefIdentityInitializer) {
            var name: LocalizedText? = null
            var description: LocalizedMarkdown? = null
            var documentationHome: URL? = null
            fun build(): EntityDefInitializer {
                return EntityDefInitializer(
                    entityKey = entityKey,
                    identityAttribute = identityAttribute,
                    name = name,
                    description = description,
                    documentationHome = documentationHome
                )
            }
        }
    }
}