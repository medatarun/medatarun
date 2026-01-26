package io.medatarun.model.adapters.json

import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import java.util.*

class ModelRefTypeJsonConverter : TypeJsonConverter<ModelRef> {
    override fun deserialize(json: JsonElement): ModelRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                ModelRef.ById(ModelId(UUID.fromString(id)))
            },
            whenKey = { key ->
                ModelRef.ByKey(ModelKey(key))
            }
        )
    }

}
