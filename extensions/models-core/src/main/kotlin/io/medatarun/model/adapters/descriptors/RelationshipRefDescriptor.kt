package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.RelationshipRefTypeJsonConverter
import io.medatarun.model.domain.RelationshipRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class RelationshipRefDescriptor : TypeDescriptor<RelationshipRef> {
    override val target: KClass<RelationshipRef> = RelationshipRef::class
    override val equivMultiplatorm: String = "RelationshipRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipRef): RelationshipRef {
        return when (value) {
            is RelationshipRef.ById -> value
            is RelationshipRef.ByKey -> {
                value.model.validated()
                value.relationship.validated()
                value
            }
        }
    }

    override val jsonConverter: TypeJsonConverter<RelationshipRef> = RelationshipRefTypeJsonConverter()

    override val description = """A reference to a relationship."""

}