package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidSearchFiltersSyntaxException
import io.medatarun.model.domain.search.*
import io.medatarun.tags.core.adapters.json.TagRefJsonConverter
import io.medatarun.types.TypeJsonConverter
import kotlinx.serialization.json.*

class SearchFiltersTypeJsonConverter : TypeJsonConverter<SearchFilters> {
    override fun deserialize(json: JsonElement): SearchFilters {
        try {
            val root = json.jsonObject
            val operatorStr = root["operator"]?.jsonPrimitive?.content
            val operator = operatorStr?.let {
                SearchFiltersLogicalOperator.valueOfCodeOptional(it)
                    ?: throw TypeJsonInvalidSearchFiltersSyntaxException(root)
            } ?: SearchFiltersLogicalOperator.AND

            val filters = root["items"]?.jsonArray?.map { json ->
                toFilter(json.jsonObject)
            } ?: emptyList()
            return SearchFilters(
                operator = operator,
                items = filters
            )
        } catch (_: Exception) {
            throw TypeJsonInvalidSearchFiltersSyntaxException(json)
        }

    }

    private fun toFilter(json: JsonObject): SearchFilter {
        val type = json["type"]?.jsonPrimitive?.content ?: throw IllegalArgumentException("Filter type is missing")
        if (type == "tags") {
            val conditionStr = json["condition"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Filter tag condition missing")
            return when (conditionStr) {
                "empty" -> SearchFilterTags.Empty
                "notEmpty" -> SearchFilterTags.NotEmpty
                "anyOf" -> SearchFilterTags.AnyOf(json["value"]?.jsonArray?.map { TagRefJsonConverter().deserialize(it) }
                    ?: emptyList())

                "noneOf" -> SearchFilterTags.NoneOf(json["value"]?.jsonArray?.map { TagRefJsonConverter().deserialize(it) }
                    ?: emptyList())

                "allOf" -> SearchFilterTags.AllOf(json["value"]?.jsonArray?.map { TagRefJsonConverter().deserialize(it) }
                    ?: emptyList())

                else -> throw IllegalArgumentException("Unknown filter tag condition: $conditionStr")
            }
        }
        if (type == "text") {
            val conditionStr = json["condition"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Filter text condition missing")
            return when (conditionStr) {
                "contains" -> SearchFilterText.Contains(json["value"]?.jsonPrimitive?.content ?: "")
                else -> throw IllegalArgumentException("Unknown filter text condition: $conditionStr")
            }
        } else throw IllegalArgumentException("Unknown filter type: $type")
    }
}
