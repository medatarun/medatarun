package io.medatarun.model.adapters

import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.internal.KeyValidation
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class RelationshipKeyDescriptor : TypeDescriptor<RelationshipKey> {
    override val target: KClass<RelationshipKey> = RelationshipKey::class
    override val equivMultiplatorm: String = "RelationshipKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipKey): RelationshipKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}