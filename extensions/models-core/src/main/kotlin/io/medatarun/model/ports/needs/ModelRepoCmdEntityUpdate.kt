package io.medatarun.model.ports.needs

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import java.net.URL

sealed class ModelRepoCmdEntityUpdate {
    data class Key(val value: EntityKey) : ModelRepoCmdEntityUpdate()
    data class Name(val value: LocalizedText?) : ModelRepoCmdEntityUpdate()
    data class Description(val value: LocalizedMarkdown?) : ModelRepoCmdEntityUpdate()
    data class IdentifierAttribute(val value: AttributeId) : ModelRepoCmdEntityUpdate()
    data class DocumentationHome(val value: URL?) : ModelRepoCmdEntityUpdate()
}