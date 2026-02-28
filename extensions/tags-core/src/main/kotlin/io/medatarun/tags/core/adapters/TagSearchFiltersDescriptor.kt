package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.adapters.json.TagSearchFiltersJsonConverter
import io.medatarun.tags.core.domain.TagSearchFilters
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TagSearchFiltersDescriptor : TypeDescriptor<TagSearchFilters> {
    override val target: KClass<TagSearchFilters> = TagSearchFilters::class
    override val equivMultiplatorm: String = "TagSearchFilters"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.OBJECT
    override val jsonConverter: TypeJsonConverter<TagSearchFilters> = TagSearchFiltersJsonConverter()

    override fun validate(value: TagSearchFilters): TagSearchFilters {
        return value
    }
}
