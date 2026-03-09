package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.ModelAuthorityTypeJsonConverter
import io.medatarun.model.domain.ModelAuthority
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class ModelAuthorityDescriptor : TypeDescriptor<ModelAuthority> {
    override val target: KClass<ModelAuthority> = ModelAuthority::class
    override val equivMultiplatorm: String = "ModelAuthority"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelAuthority): ModelAuthority {
        return value
    }

    override val description: String = "Canonical models are authoritative business references. System models describe imported implementations."
    override val jsonConverter: TypeJsonConverter<ModelAuthority> = ModelAuthorityTypeJsonConverter()
}
