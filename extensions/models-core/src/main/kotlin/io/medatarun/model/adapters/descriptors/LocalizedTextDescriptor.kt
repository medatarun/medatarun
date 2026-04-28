package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.LocalizedTextTypeJsonConverter
import io.medatarun.model.domain.TEXT_SINGLE_LINE_DESCRIPTION
import io.medatarun.model.domain.LocalizedText
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class LocalizedTextDescriptor : TypeDescriptor<LocalizedText> {
    override val target: KClass<LocalizedText> = LocalizedText::class
    override val equivMultiplatorm: String = "LocalizedText"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: LocalizedText): LocalizedText {
        return value.validate()
    }

    override val description: String = TEXT_SINGLE_LINE_DESCRIPTION
    override val jsonConverter: TypeJsonConverter<LocalizedText> = LocalizedTextTypeJsonConverter()
}