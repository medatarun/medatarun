package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.domain.TagKey
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TagKeyTypeDescriptor : TypeDescriptor<TagKey> {
    override val target: KClass<TagKey> = TagKey::class
    override val equivMultiplatorm: String = "TagKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TagKey): TagKey {
        return value.validated()
    }
}
