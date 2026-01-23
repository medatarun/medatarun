package io.medatarun.model.adapters

import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class EntityAttributeRefDescriptor : TypeDescriptor<EntityAttributeRef> {
    override val target: KClass<EntityAttributeRef> = EntityAttributeRef::class
    override val equivMultiplatorm: String = "EntityAttributeRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: EntityAttributeRef): EntityAttributeRef {
       return when (value) {
           is EntityAttributeRef.ById -> value
           is EntityAttributeRef.ByKey -> {
               value.model.validated()
               value.entity.validated()
               value.attribute.validated()
               value
           }
       }
    }

    override val jsonConverter: TypeJsonConverter<EntityAttributeRef> = EntityAttributeRefTypeJsonConverter()


    override val description = """A reference to an entity attribute."""

}