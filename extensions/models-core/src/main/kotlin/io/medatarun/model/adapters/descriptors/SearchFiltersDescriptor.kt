package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.SearchFiltersTypeJsonConverter
import io.medatarun.model.domain.search.SearchFilters
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class SearchFiltersDescriptor : TypeDescriptor<SearchFilters> {
    override val target: KClass<SearchFilters> = SearchFilters::class

    override fun validate(value: SearchFilters): SearchFilters {
        return value
    }

    override val equivMultiplatorm: String = "SearchFilters"


    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.OBJECT

    override val jsonConverter: TypeJsonConverter<SearchFilters>
        get() = SearchFiltersTypeJsonConverter()
}