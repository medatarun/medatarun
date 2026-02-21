package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagFreeRef
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class TagFreeRefJsonConverter : TypeJsonConverter<TagFreeRef> {
    override fun deserialize(json: JsonElement): TagFreeRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                TagFreeRef.ById(TagFreeId.fromString(id))
            },
            whenKey = { key ->
                TagFreeRef.ByKey(TagFreeKey(key))
            }
        )
    }
}
