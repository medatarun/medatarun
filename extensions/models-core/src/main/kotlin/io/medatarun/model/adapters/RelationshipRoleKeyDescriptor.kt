package io.medatarun.model.adapters

import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.internal.KeyValidation
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class RelationshipRoleKeyDescriptor : TypeDescriptor<RelationshipRoleKey> {
    override val target: KClass<RelationshipRoleKey> = RelationshipRoleKey::class
    override val equivMultiplatorm: String = "RelationshipRoleKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipRoleKey): RelationshipRoleKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}