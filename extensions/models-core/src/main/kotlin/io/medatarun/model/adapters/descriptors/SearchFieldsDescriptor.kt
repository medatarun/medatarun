package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.SearchFieldsTypeJsonConverter
import io.medatarun.model.domain.search.SearchFields
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class SearchFieldsDescriptor : TypeDescriptor<SearchFields> {
    override val target: KClass<SearchFields> = SearchFields::class

    override fun validate(value: SearchFields): SearchFields {
        return value
    }

    override val equivMultiplatorm: String = "SearchFields"

    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.ARRAY

    override val jsonConverter: TypeJsonConverter<SearchFields> = SearchFieldsTypeJsonConverter()
}