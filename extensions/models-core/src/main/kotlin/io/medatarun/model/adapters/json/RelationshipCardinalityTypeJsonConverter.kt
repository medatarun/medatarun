package io.medatarun.model.adapters.json

import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class RelationshipCardinalityTypeJsonConverter : TypeJsonConverter<RelationshipCardinality> {
    override fun deserialize(json: JsonElement): RelationshipCardinality {
        return RefTypeJsonConverters.expectingString(json) {
            RelationshipCardinality.valueOfCode(it)
        }

    }
}
