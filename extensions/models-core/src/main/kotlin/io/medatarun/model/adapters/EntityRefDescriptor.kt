package io.medatarun.model.adapters

import io.medatarun.model.domain.EntityRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class EntityRefDescriptor : TypeDescriptor<EntityRef> {
    override val target: KClass<EntityRef> = EntityRef::class
    override val equivMultiplatorm: String = "EntityRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: EntityRef): EntityRef {
        return when (value) {
            is EntityRef.ById -> value
            is EntityRef.ByKey -> {
                value.model.validated()
                value.entity.validated()
                value
            }
        }
    }

    override val jsonConverter = EntityRefTypeJsonConverter()

    override val description = """A reference to an entity attribute."""

}