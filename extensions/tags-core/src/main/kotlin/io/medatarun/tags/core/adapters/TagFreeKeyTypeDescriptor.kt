package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TagFreeKeyTypeDescriptor : TypeDescriptor<TagFreeKey> {
    override val target: KClass<TagFreeKey> = TagFreeKey::class
    override val equivMultiplatorm: String = "TagFreeKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TagFreeKey): TagFreeKey {
        return value.validated()
    }
}