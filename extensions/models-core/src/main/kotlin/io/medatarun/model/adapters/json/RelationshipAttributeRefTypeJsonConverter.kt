package io.medatarun.model.adapters.json

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.RelationshipAttributeRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import java.util.*

class RelationshipAttributeRefTypeJsonConverter : TypeJsonConverter<RelationshipAttributeRef> {
    override fun deserialize(json: JsonElement): RelationshipAttributeRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                RelationshipAttributeRef.ById(AttributeId(UUID.fromString(id)))
            },
            whenKey = { key ->
                RelationshipAttributeRef.ByKey(AttributeKey(key))
            }
        )
    }
}
