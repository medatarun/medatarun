package io.medatarun.model.adapters

import io.medatarun.model.domain.EntityKey
import io.medatarun.model.internal.KeyValidation
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class EntityKeyDescriptor : TypeDescriptor<EntityKey> {
    override val target: KClass<EntityKey> = EntityKey::class
    override val equivMultiplatorm: String = "EntityKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: EntityKey): EntityKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION

}