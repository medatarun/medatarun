package io.medatarun.tags.core.adapters.types

import io.medatarun.tags.core.adapters.json.TagScopeRefJsonConverter
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv

class TagScopeRefTypeDescriptor : TypeDescriptor<TagScopeRef> {
    override val target = TagScopeRef::class
    override val equivMultiplatorm: String = "TagScopeRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.OBJECT
    override val description = """A reference to a tag scope."""
    override val jsonConverter: TypeJsonConverter<TagScopeRef> = TagScopeRefJsonConverter()

    override fun validate(value: TagScopeRef): TagScopeRef {
        return when (value) {
            is TagScopeRef.Global -> value
            is TagScopeRef.Local -> value
        }
    }
}
