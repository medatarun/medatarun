package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.domain.TagFreeId
import io.medatarun.tags.core.domain.TagFreeKey
import io.medatarun.tags.core.domain.TagFreeRef
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.tags.core.domain.TagManagedId
import io.medatarun.tags.core.domain.TagManagedKey
import io.medatarun.tags.core.domain.TagManagedRef
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement

class TagManagedRefJsonConverter : TypeJsonConverter<TagManagedRef> {
    override fun deserialize(json: JsonElement): TagManagedRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                TagManagedRef.ById(TagManagedId.fromString(id))
            },
            whenKey = { key ->
                TagManagedRef.ByKey(TagManagedKey(key))
            }
        )
    }
}
