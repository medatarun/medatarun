package io.medatarun.model.adapters

import io.medatarun.model.domain.TypeKey
import io.medatarun.model.internal.KeyValidation
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TypeKeyDescriptor : TypeDescriptor<TypeKey> {
    override val target: KClass<TypeKey> = TypeKey::class
    override val equivMultiplatorm: String = "TypeKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TypeKey): TypeKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}