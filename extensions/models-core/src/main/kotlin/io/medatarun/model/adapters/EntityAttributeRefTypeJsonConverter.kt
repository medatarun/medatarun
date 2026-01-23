package io.medatarun.model.adapters

import io.medatarun.model.adapters.RefTypeJsonConverters.decodeRef
import io.medatarun.model.domain.*
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import java.util.*

class EntityAttributeRefTypeJsonConverter : TypeJsonConverter<EntityAttributeRef> {
    override fun deserialize(json: JsonElement): EntityAttributeRef {
        return decodeRef(
            json,
            whenId = { id ->
                EntityAttributeRef.ById(AttributeId(UUID.fromString(id)))
            },
            whenKey = { keyParts ->
                EntityAttributeRef.ByKey(
                    model = ModelKey(keyParts.required("model")),
                    entity = EntityKey(keyParts.required("entity")),
                    attribute = AttributeKey(keyParts.required("attribute")),
                )
            }
        )
    }
}
