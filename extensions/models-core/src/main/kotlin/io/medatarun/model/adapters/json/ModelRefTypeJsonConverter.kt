package io.medatarun.model.adapters.json

import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class ModelRefTypeJsonConverter : TypeJsonConverter<ModelRef> {
    override fun deserialize(json: JsonElement): ModelRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                ModelRef.ById(ModelId.fromString(id))
            },
            whenKey = { key ->
                ModelRef.ByKey(ModelKey(key))
            }
        )
    }

}
