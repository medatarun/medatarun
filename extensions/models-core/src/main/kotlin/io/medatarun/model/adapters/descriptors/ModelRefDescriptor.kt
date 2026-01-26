package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.ModelRefTypeJsonConverter
import io.medatarun.model.domain.ModelRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class ModelRefDescriptor : TypeDescriptor<ModelRef> {
    override val target: KClass<ModelRef> = ModelRef::class
    override val equivMultiplatorm: String = "ModelRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelRef): ModelRef {
        return when (value) {
            is ModelRef.ById -> value
            is ModelRef.ByKey -> {
                value.key.validated()
                value
            }
        }
    }

    override val description = """A reference to an entity attribute."""

    override val jsonConverter: TypeJsonConverter<ModelRef> = ModelRefTypeJsonConverter()
}