package io.medatarun.model.adapters.json

import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class EntityRefTypeJsonConverter : TypeJsonConverter<EntityRef> {
    override fun deserialize(json: JsonElement): EntityRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                EntityRef.ById(EntityId.fromString(id))
            },
            whenKey = { key ->
                EntityRef.ByKey(EntityKey(key))
            }
        )
    }

}
