package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import java.net.URL

data class EntityInitializer(
    val entityKey: EntityKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identityAttribute: AttributeIdentityInitializer,
    val documentationHome: URL?
) {
    companion object {
        fun build(entityKey: EntityKey, identityAttribute: AttributeIdentityInitializer, block: Builder.() -> Unit = {}): EntityInitializer {
            return Builder(entityKey, identityAttribute).apply(block).build()
        }
        class Builder(var entityKey: EntityKey, var identityAttribute: AttributeIdentityInitializer) {
            var name: LocalizedText? = null
            var description: LocalizedMarkdown? = null
            var documentationHome: URL? = null
            fun build(): EntityInitializer {
                return EntityInitializer(
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