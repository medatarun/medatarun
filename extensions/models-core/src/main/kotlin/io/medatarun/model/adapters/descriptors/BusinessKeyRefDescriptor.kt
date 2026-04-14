package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.BusinessKeyRefTypeJsonConverter
import io.medatarun.model.adapters.json.EntityRefTypeJsonConverter
import io.medatarun.model.domain.BusinessKeyRef
import io.medatarun.model.domain.EntityRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class BusinessKeyRefDescriptor : TypeDescriptor<BusinessKeyRef> {
    override val target: KClass<BusinessKeyRef> = BusinessKeyRef::class
    override val equivMultiplatorm: String = "BusinessKeyRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: BusinessKeyRef): BusinessKeyRef {
        return when (value) {
            is BusinessKeyRef.ById -> value
            is BusinessKeyRef.ByKey -> {
                value.key.validated()
                value
            }
        }
    }

    override val jsonConverter = BusinessKeyRefTypeJsonConverter()

    override val description = """A reference to a business key."""

}