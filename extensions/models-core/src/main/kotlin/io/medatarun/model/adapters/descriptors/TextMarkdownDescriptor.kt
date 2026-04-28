package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.TextMarkdownTypeJsonConverter
import io.medatarun.model.domain.TEXT_MARKDOWN_DESCRIPTION
import io.medatarun.model.domain.TextMarkdown
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TextMarkdownDescriptor : TypeDescriptor<TextMarkdown> {
    override val target: KClass<TextMarkdown> = TextMarkdown::class
    override val equivMultiplatorm: String = "TextMarkdown"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TextMarkdown): TextMarkdown {
        return value.validate()
    }

    override val description: String = TEXT_MARKDOWN_DESCRIPTION
    override val jsonConverter: TypeJsonConverter<TextMarkdown> = TextMarkdownTypeJsonConverter()
}