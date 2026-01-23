package io.medatarun.model.adapters

import io.medatarun.model.domain.RelationshipRoleRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class RelationshipRoleRefDescriptor : TypeDescriptor<RelationshipRoleRef> {
    override val target: KClass<RelationshipRoleRef> = RelationshipRoleRef::class
    override val equivMultiplatorm: String = "RelationshipRoleRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipRoleRef): RelationshipRoleRef {
        return when (value) {
            is RelationshipRoleRef.ById -> value
            is RelationshipRoleRef.ByKey -> {
                value.model.validated()
                value.relationship.validated()
                value.role.validated()
                value
            }
        }
    }

    override val jsonConverter: TypeJsonConverter<RelationshipRoleRef> = RelationshipRoleRefTypeJsonConverter()
    override val description = """A reference to a relationship role."""

}