package io.medatarun.model.adapters.json

import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeRef
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import java.util.*

class TypeRefTypeJsonConverter : TypeJsonConverter<TypeRef> {
    override fun deserialize(json: JsonElement): TypeRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id -> TypeRef.ById(TypeId(UUID.fromString(id))) },
            whenKey = { key -> TypeRef.ByKey(TypeKey(key)) }
        )
    }

}
