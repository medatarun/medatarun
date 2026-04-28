package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.TextSingleLineJsonConverter
import io.medatarun.model.domain.TEXT_SINGLE_LINE_DESCRIPTION
import io.medatarun.model.domain.TextSingleLine
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TextSingleLineDescriptor : TypeDescriptor<TextSingleLine> {
    override val target: KClass<TextSingleLine> = TextSingleLine::class
    override val equivMultiplatorm: String = "TextSingleLine"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TextSingleLine): TextSingleLine {
        return value.validate()
    }

    override val description: String = TEXT_SINGLE_LINE_DESCRIPTION
    override val jsonConverter: TypeJsonConverter<TextSingleLine> = TextSingleLineJsonConverter()
}