package io.medatarun.tags.core.adapters.types

import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv

class TagGroupKeyTypeDescriptor : TypeDescriptor<TagGroupKey> {
    override val target = TagGroupKey::class
    override val equivMultiplatorm: String = "TagGroupKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TagGroupKey): TagGroupKey {
        return value.validated()
    }
}
