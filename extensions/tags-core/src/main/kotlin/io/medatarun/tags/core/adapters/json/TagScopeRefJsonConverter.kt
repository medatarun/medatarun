package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.type.commons.id.Id
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class TagScopeRefJsonConverter : TypeJsonConverter<TagScopeRef> {
    override fun deserialize(json: JsonElement): TagScopeRef {
        val obj = json as? JsonObject ?: throw TypeJsonConverterBadFormatException("Expected object for TagScopeRef")
        val typeValue = readString(obj, "type")
        if (typeValue == TagScopeRef.Global.type.value) {
            return TagScopeRef.Global
        }
        val idValue = readString(obj, "id")
        return TagScopeRef.Local(
            type = TagScopeType(typeValue),
            localScopeId = Id.fromString(idValue, ::TagScopeId)
        )
    }

    private fun readString(obj: JsonObject, key: String): String {
        val primitive = obj[key] as? JsonPrimitive ?: throw TypeJsonConverterBadFormatException("Missing field '$key'")
        if (!primitive.isString) {
            throw TypeJsonConverterBadFormatException("Field '$key' must be a string")
        }
        return primitive.content
    }
}
