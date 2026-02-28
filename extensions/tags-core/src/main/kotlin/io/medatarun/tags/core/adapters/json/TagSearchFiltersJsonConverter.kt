package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.adapters.TypeJsonInvalidTagSearchFiltersSyntaxException
import io.medatarun.tags.core.domain.TagSearchFilter
import io.medatarun.tags.core.domain.TagSearchFilterScopeRef
import io.medatarun.tags.core.domain.TagSearchFilters
import io.medatarun.tags.core.domain.TagSearchFiltersLogicalOperator
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.*

class TagSearchFiltersJsonConverter : TypeJsonConverter<TagSearchFilters> {
    override fun deserialize(json: JsonElement): TagSearchFilters {
        try {
            val root = json.jsonObject
            val operatorString = root["operator"]?.jsonPrimitive?.content
            val operator = operatorString?.let {
                TagSearchFiltersLogicalOperator.valueOfCodeOptional(it)
                    ?: throw TypeJsonInvalidTagSearchFiltersSyntaxException(root)
            } ?: TagSearchFiltersLogicalOperator.AND

            val items = root["items"]?.jsonArray?.map {
                toFilter(it.jsonObject)
            } ?: emptyList()

            return TagSearchFilters(
                operator = operator,
                items = items
            )
        } catch (_: Exception) {
            throw TypeJsonInvalidTagSearchFiltersSyntaxException(json)
        }
    }

    private fun toFilter(json: JsonObject): TagSearchFilter {
        val type = json["type"]?.jsonPrimitive?.content
            ?: throw TypeJsonInvalidTagSearchFiltersSyntaxException(json)
        if (type == "scopeRef") {
            val condition = json["condition"]?.jsonPrimitive?.content
                ?: throw TypeJsonInvalidTagSearchFiltersSyntaxException(json)
            if (condition == "is") {
                val value = json["value"] ?: throw TypeJsonInvalidTagSearchFiltersSyntaxException(json)
                return TagSearchFilterScopeRef.Is(TagScopeRefJsonConverter().deserialize(value))
            }
            throw TypeJsonInvalidTagSearchFiltersSyntaxException(json)
        }
        throw TypeJsonInvalidTagSearchFiltersSyntaxException(json)
    }
}
