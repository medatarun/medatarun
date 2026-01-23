package io.medatarun.model.adapters

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.internal.KeyValidation
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class AttributeKeyDescriptor : TypeDescriptor<AttributeKey> {
    override val target: KClass<AttributeKey> = AttributeKey::class
    override val equivMultiplatorm: String = "AttributeKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: AttributeKey): AttributeKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION

}