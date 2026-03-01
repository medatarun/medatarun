package io.medatarun.tags.core.adapters.json

import io.medatarun.tags.core.adapters.TypeJsonInvalidTagSearchFiltersSyntaxException
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.domain.TagSearchFilterScopeRef
import io.medatarun.tags.core.domain.TagSearchFilters
import io.medatarun.tags.core.domain.TagSearchFiltersLogicalOperator
import io.medatarun.type.commons.id.Id
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TagSearchFiltersJsonConverterTest {

    private val converter = TagSearchFiltersJsonConverter()

    @Test
    fun `deserialize should use and operator by default and return empty items when omitted`() {
        val result = converter.deserialize(Json.parseToJsonElement("{}"))

        assertEquals(
            TagSearchFilters(
                operator = TagSearchFiltersLogicalOperator.AND,
                items = emptyList()
            ),
            result
        )
    }

    @Test
    fun `deserialize should decode scope ref filter`() {
        val result = converter.deserialize(
            Json.parseToJsonElement(
                """
                {
                  "operator": "or",
                  "items": [
                    {
                      "type": "scopeRef",
                      "condition": "is",
                      "value": {
                        "type": "model",
                        "id": "11111111-2222-3333-4444-555555555555"
                      }
                    }
                  ]
                }
                """.trimIndent()
            )
        )

        assertEquals(
            TagSearchFilters(
                operator = TagSearchFiltersLogicalOperator.OR,
                items = listOf(
                    TagSearchFilterScopeRef.Is(
                        TagScopeRef.Local(
                            type = TagScopeType("model"),
                            localScopeId = Id.fromString(
                                "11111111-2222-3333-4444-555555555555",
                                ::TagScopeId
                            )
                        )
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `deserialize should reject unknown operator`() {
        assertFailsWith<TypeJsonInvalidTagSearchFiltersSyntaxException> {
            converter.deserialize(
                Json.parseToJsonElement(
                    """
                    {
                      "operator": "xor"
                    }
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `deserialize should reject missing filter type`() {
        assertFailsWith<TypeJsonInvalidTagSearchFiltersSyntaxException> {
            converter.deserialize(
                Json.parseToJsonElement(
                    """
                    {
                      "items": [
                        {
                          "condition": "is",
                          "value": {
                            "type": "global"
                          }
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `deserialize should reject unknown filter type`() {
        assertFailsWith<TypeJsonInvalidTagSearchFiltersSyntaxException> {
            converter.deserialize(
                Json.parseToJsonElement(
                    """
                    {
                      "items": [
                        {
                          "type": "tagRef"
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `deserialize should reject missing scope ref condition`() {
        assertFailsWith<TypeJsonInvalidTagSearchFiltersSyntaxException> {
            converter.deserialize(
                Json.parseToJsonElement(
                    """
                    {
                      "items": [
                        {
                          "type": "scopeRef",
                          "value": {
                            "type": "global"
                          }
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `deserialize should reject unknown scope ref condition`() {
        assertFailsWith<TypeJsonInvalidTagSearchFiltersSyntaxException> {
            converter.deserialize(
                Json.parseToJsonElement(
                    """
                    {
                      "items": [
                        {
                          "type": "scopeRef",
                          "condition": "contains",
                          "value": {
                            "type": "global"
                          }
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `deserialize should reject missing scope ref value`() {
        assertFailsWith<TypeJsonInvalidTagSearchFiltersSyntaxException> {
            converter.deserialize(
                Json.parseToJsonElement(
                    """
                    {
                      "items": [
                        {
                          "type": "scopeRef",
                          "condition": "is"
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
        }
    }
}
