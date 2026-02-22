package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.adapters.json.TagGroupRefJsonConverter
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv

class TagGroupRefTypeDescriptor : TypeDescriptor<TagGroupRef> {
    override val target = TagGroupRef::class
    override val equivMultiplatorm: String = "TagGroupRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override val description = """A reference to tag group."""
    override val jsonConverter: TypeJsonConverter<TagGroupRef> = TagGroupRefJsonConverter()
    override fun validate(value: TagGroupRef): TagGroupRef {
        return when (value) {
            is TagGroupRef.ById -> value
            is TagGroupRef.ByKey -> {
                value.key.validated()
                value
            }
        }
    }
}
