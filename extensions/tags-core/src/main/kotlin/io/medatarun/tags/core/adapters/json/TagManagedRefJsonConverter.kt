package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.domain.TagManagedId
import io.medatarun.tags.core.domain.TagManagedKey
import io.medatarun.tags.core.domain.TagManagedRef
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class TagManagedRefJsonConverter : TypeJsonConverter<TagManagedRef> {
    override fun deserialize(json: JsonElement): TagManagedRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                TagManagedRef.ById(Id.fromString(id, ::TagManagedId))
            },
            whenKey = { key ->
                TagManagedRef.ByKey(Key.fromString(key, ::TagManagedKey))
            }
        )
    }
}
