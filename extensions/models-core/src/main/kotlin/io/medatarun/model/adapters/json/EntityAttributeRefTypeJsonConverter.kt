package io.medatarun.model.adapters.json

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class EntityAttributeRefTypeJsonConverter : TypeJsonConverter<EntityAttributeRef> {
    override fun deserialize(json: JsonElement): EntityAttributeRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                EntityAttributeRef.ById(AttributeId.fromString(id))
            },
            whenKey = { key -> EntityAttributeRef.ByKey(AttributeKey(key)) }
        )
    }
}
