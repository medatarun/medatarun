package io.medatarun.model.adapters.json

import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.ModelKey
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import java.util.*

class EntityRefTypeJsonConverter : TypeJsonConverter<EntityRef> {
    override fun deserialize(json: JsonElement): EntityRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                EntityRef.ById(EntityId(UUID.fromString(id)))
            },
            whenKey = { keyParts ->
                EntityRef.ByKey(
                    model = ModelKey(keyParts.required("model")),
                    entity = EntityKey(keyParts.required("entity")),
                )
            }
        )
    }

}
