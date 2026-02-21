package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagManagedKey
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TagManagedKeyTypeDescriptor : TypeDescriptor<TagManagedKey> {
    override val target = TagManagedKey::class
    override val equivMultiplatorm: String = "TagManagedKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TagManagedKey): TagManagedKey {
        return value.validated()
    }
}