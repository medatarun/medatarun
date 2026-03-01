package io.medatarun.tags.core.adapters

import io.medatarun.tags.core.domain.TagId
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TagIdTypeDescriptor : TypeDescriptor<TagId> {
    override val target: KClass<TagId> = TagId::class
    override val equivMultiplatorm: String = "TagId"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TagId): TagId {
        return value
    }
}
