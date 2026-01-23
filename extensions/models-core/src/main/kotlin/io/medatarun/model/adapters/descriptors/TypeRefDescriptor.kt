package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.TypeRefTypeJsonConverter
import io.medatarun.model.domain.TypeRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class TypeRefDescriptor : TypeDescriptor<TypeRef> {
    override val target: KClass<TypeRef> = TypeRef::class
    override val equivMultiplatorm: String = "TypeRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TypeRef): TypeRef {
        return when (value) {
            is TypeRef.ById -> value
            is TypeRef.ByKey -> {
                value.model.validated()
                value.type.validated()
                value
            }
        }
    }

    override val jsonConverter: TypeJsonConverter<TypeRef> = TypeRefTypeJsonConverter()
    override val description = """A reference to a relationship role."""

}