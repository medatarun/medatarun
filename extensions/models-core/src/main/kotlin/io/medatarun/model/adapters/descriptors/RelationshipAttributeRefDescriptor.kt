package io.medatarun.model.adapters.descriptors

import io.medatarun.model.adapters.json.RelationshipAttributeRefTypeJsonConverter
import io.medatarun.model.domain.RelationshipAttributeRef
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KClass

class RelationshipAttributeRefDescriptor : TypeDescriptor<RelationshipAttributeRef> {
    override val target: KClass<RelationshipAttributeRef> = RelationshipAttributeRef::class
    override val equivMultiplatorm: String = "RelationshipAttributeRef"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipAttributeRef): RelationshipAttributeRef {
        return when (value) {
            is RelationshipAttributeRef.ById -> value
            is RelationshipAttributeRef.ByKey -> {
                value.key.validated()
                value
            }
        }
    }

    override val jsonConverter = RelationshipAttributeRefTypeJsonConverter()

    override val description = """A reference to a relationship attribute."""

}