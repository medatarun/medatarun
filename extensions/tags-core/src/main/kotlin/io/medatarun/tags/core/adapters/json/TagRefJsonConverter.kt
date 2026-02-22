package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key
import io.medatarun.type.commons.ref.RefTypeJsonConverters
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import kotlinx.serialization.json.JsonElement

class TagRefJsonConverter : TypeJsonConverter<TagRef> {
    override fun deserialize(json: JsonElement): TagRef {
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                TagRef.ById(Id.fromString(id, ::TagId))
            },
            whenKey = { key ->
                decodeTagKeyRef(key)
            }
        )
    }

    private fun decodeTagKeyRef(value: String): TagRef.ByKey {
        val parts = value.split('/')
        if (parts.size != 3) {
            throw TypeJsonConverterBadFormatException("Invalid tag ref key format: $value")
        }
        val scopeTypeValue = parts[0]
        val middleValue = parts[1]
        val tagKeyValue = parts[2]
        if (scopeTypeValue.isBlank() || middleValue.isBlank() || tagKeyValue.isBlank()) {
            throw TypeJsonConverterBadFormatException("Invalid tag ref key format: $value")
        }
        val key = Key.fromString(tagKeyValue, ::TagKey).validated()
        if (scopeTypeValue == TagScopeRef.Global.type.value) {
            return TagRef.ByKey(
                scopeRef = TagScopeRef.Global,
                groupKey = Key.fromString(middleValue, ::TagGroupKey).validated(),
                key = key
            )
        }
        return TagRef.ByKey(
            scopeRef = TagScopeRef.Local(
                type = TagScopeType(scopeTypeValue),
                localScopeId = Id.fromString(middleValue, ::TagScopeId)
            ),
            groupKey = null,
            key = key
        )
    }
}
