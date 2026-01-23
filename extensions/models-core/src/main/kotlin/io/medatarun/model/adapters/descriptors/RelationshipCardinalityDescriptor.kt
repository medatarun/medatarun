package io.medatarun.model.adapters.descriptors

import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonEquiv
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KClass

class RelationshipCardinalityDescriptor : TypeDescriptor<RelationshipCardinality> {
    override val target: KClass<RelationshipCardinality> = RelationshipCardinality::class
    override val equivMultiplatorm: String = "RelationshipCardinality"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipCardinality): RelationshipCardinality {
        return value
    }

    override val description: String = ""
    override val jsonConverter: TypeJsonConverter<RelationshipCardinality> = object :
        TypeJsonConverter<RelationshipCardinality> {
        override fun deserialize(json: JsonElement): RelationshipCardinality {
            when(json) {
                is JsonPrimitive -> return RelationshipCardinality.Companion.valueOfCode(json.content)
                else -> throw TypeJsonConverterBadFormatException("Expecting a String")
            }
        }

    }

}