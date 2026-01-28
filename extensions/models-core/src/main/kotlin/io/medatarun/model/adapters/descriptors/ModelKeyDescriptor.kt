package io.medatarun.model.adapters.descriptors

import io.medatarun.model.domain.ModelKey
import io.medatarun.model.internal.KeyValidation
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class ModelKeyDescriptor : TypeDescriptor<ModelKey> {
    override val target: KClass<ModelKey> = ModelKey::class
    override val equivMultiplatorm: String = "ModelKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelKey): ModelKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}