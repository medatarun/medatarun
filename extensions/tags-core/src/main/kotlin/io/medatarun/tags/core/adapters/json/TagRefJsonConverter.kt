package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagRef
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
        val slashIndex = value.indexOf('/')
        if (slashIndex < 0) {
            return TagRef.ByKey(
                groupKey = null,
                key = Key.fromString(value, ::TagKey).validated()
            )
        }
        if (slashIndex == 0 || slashIndex == value.length - 1) {
            throw TypeJsonConverterBadFormatException("Invalid tag ref key format: $value")
        }
        if (value.indexOf('/', slashIndex + 1) >= 0) {
            throw TypeJsonConverterBadFormatException("Invalid tag ref key format: $value")
        }
        val groupKeyValue = value.substring(0, slashIndex)
        val tagKeyValue = value.substring(slashIndex + 1)
        return TagRef.ByKey(
            groupKey = Key.fromString(groupKeyValue, ::TagGroupKey).validated(),
            key = Key.fromString(tagKeyValue, ::TagKey).validated()
        )
    }
}
