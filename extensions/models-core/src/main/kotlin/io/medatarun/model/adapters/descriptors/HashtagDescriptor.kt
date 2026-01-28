package io.medatarun.model.adapters.descriptors

import io.medatarun.model.domain.Hashtag
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class HashtagDescriptor : TypeDescriptor<Hashtag> {
    override val target: KClass<Hashtag> = Hashtag::class
    override val equivMultiplatorm: String = "Hashtag"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: Hashtag): Hashtag {
        return value.validated()
    }
}