package io.medatarun.model.adapters.json

import io.medatarun.model.domain.*
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
            whenKey = { keyParts ->
                RelationshipAttributeRef.ByKey(
                    model = ModelKey(keyParts.required("model")),
                    relationship = RelationshipKey(keyParts.required("relationship")),
                    attribute = AttributeKey(keyParts.required("attribute")),
                )
            }
        )
    }
}
