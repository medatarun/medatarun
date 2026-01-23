package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.LocalizedMarkdownTypeJsonConverter
import io.medatarun.model.domain.LOCALIZED_MARKDOWN_DESCRIPTION
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class LocalizedMarkdownDescriptor : TypeDescriptor<LocalizedMarkdown> {
    override val target: KClass<LocalizedMarkdown> = LocalizedMarkdown::class
    override val equivMultiplatorm: String = "LocalizedMarkdown"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: LocalizedMarkdown): LocalizedMarkdown {
        return value.validate()
    }
    override val description: String = LOCALIZED_MARKDOWN_DESCRIPTION
    override val jsonConverter: TypeJsonConverter<LocalizedMarkdown> = LocalizedMarkdownTypeJsonConverter()
}