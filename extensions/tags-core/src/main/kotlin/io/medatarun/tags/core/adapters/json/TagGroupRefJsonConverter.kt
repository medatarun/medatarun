package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class TagGroupRefJsonConverter : TypeJsonConverter<TagGroupRef> {
    override fun deserialize(json: JsonElement): TagGroupRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                TagGroupRef.ById(Id.fromString(id, ::TagGroupId))
            },
            whenKey = { key ->
                TagGroupRef.ByKey(Key.fromString(key, ::TagGroupKey))
            }
        )
    }
}
