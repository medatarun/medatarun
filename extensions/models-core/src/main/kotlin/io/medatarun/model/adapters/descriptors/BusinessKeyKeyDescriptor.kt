package io.medatarun.model.adapters.descriptors

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.BusinessKeyKey
import io.medatarun.type.commons.key.KeyValidation
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class BusinessKeyKeyDescriptor : TypeDescriptor<BusinessKeyKey> {
    override val target: KClass<BusinessKeyKey> = BusinessKeyKey::class
    override val equivMultiplatorm: String = "BusinessKeyKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: BusinessKeyKey): BusinessKeyKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION

}