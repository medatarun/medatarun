package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagManagedKey
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TagGroupKeyTypeDescriptor : TypeDescriptor<TagGroupKey> {
    override val target = TagGroupKey::class
    override val equivMultiplatorm: String = "TagGroupKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TagGroupKey): TagGroupKey {
        return value.validated()
    }
}