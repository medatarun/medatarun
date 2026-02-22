package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.adapters.json.TagManagedRefJsonConverter
import io.medatarun.tags.core.domain.TagManagedRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv

class TagManagedRefTypeDescriptor : TypeDescriptor<TagManagedRef> {
    override val target = TagManagedRef::class
    override val equivMultiplatorm: String = "TagManagedRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override val description = """A reference to managed tag."""
    override val jsonConverter: TypeJsonConverter<TagManagedRef> = TagManagedRefJsonConverter()
    override fun validate(value: TagManagedRef): TagManagedRef {
        return when (value) {
            is TagManagedRef.ById -> value
            is TagManagedRef.ByKey -> {
                value.key.validated()
                value
            }
        }
    }
}
