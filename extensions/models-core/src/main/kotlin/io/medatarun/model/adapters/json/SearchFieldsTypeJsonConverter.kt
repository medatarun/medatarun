package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidSearchFieldSyntaxException
import io.medatarun.model.domain.search.SearchField
import io.medatarun.model.domain.search.SearchFields
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class SearchFieldsTypeJsonConverter : TypeJsonConverter<SearchFields> {
    override fun deserialize(json: JsonElement): SearchFields {
        try {
            val fields: List<SearchField> = json.jsonArray.map { fieldElement ->
                val fieldName = fieldElement.jsonPrimitive.content
                SearchField.valueOfCodeOptional(fieldName) ?: throw TypeJsonInvalidSearchFieldSyntaxException(json)
            }
            return SearchFields(
                fields = fields
            )
        } catch (_: Exception) {
            throw TypeJsonInvalidSearchFieldSyntaxException(json)
        }
    }
}