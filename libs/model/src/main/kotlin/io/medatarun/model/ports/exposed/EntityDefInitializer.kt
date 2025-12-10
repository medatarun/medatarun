package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import java.net.URL

data class EntityDefInitializer(
    val entityDefId: EntityDefId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identityAttribute: AttributeDefIdentityInitializer,
    val documentationHome: URL?
) {
    companion object {
        fun build(entityDefId: EntityDefId, identityAttribute: AttributeDefIdentityInitializer, block: Builder.() -> Unit = {}): EntityDefInitializer {
            return Builder(entityDefId, identityAttribute).apply(block).build()
        }
        class Builder(var entityDefId: EntityDefId, var identityAttribute: AttributeDefIdentityInitializer) {
            var name: LocalizedText? = null
            var description: LocalizedMarkdown? = null
            var documentationHome: URL? = null
            fun build(): EntityDefInitializer {
                return EntityDefInitializer(
                    entityDefId = entityDefId,
                    identityAttribute = identityAttribute,
                    name = name,
                    description = description,
                    documentationHome = documentationHome
                )
            }
        }
    }
}