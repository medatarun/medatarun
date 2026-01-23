package io.medatarun.model.adapters.json

import io.medatarun.model.domain.LocalizedMarkdownMap
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocalizedMarkdownTypeJsonConverterTest {

    private val converter = LocalizedMarkdownTypeJsonConverter()

    @Test
    fun `deserialize should accept string and object formats`() {
        val text = converter.deserialize(JsonPrimitive("**hello**"))
        assertEquals(LocalizedMarkdownNotLocalized("**hello**"), text)

        // Contract: object values map locale -> string.
        val map = converter.deserialize(
            JsonObject(
                mapOf(
                    "en" to JsonPrimitive("**hello**"),
                    "fr" to JsonPrimitive("_salut_"),
                )
            )
        )
        val expectedMap = LocalizedMarkdownMap(mapOf("en" to "**hello**", "fr" to "_salut_"))
        assertEquals(expectedMap, map)
    }

    @Test
    fun `deserialize should reject non string or object inputs`() {
        assertFailsWith<TypeJsonConverterIllegalNullException> {
            converter.deserialize(JsonNull)
        }

        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonArray(emptyList()))
        }

        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive(12))
        }
    }

    @Test
    fun `deserialize should reject invalid object contents`() {
        // Contract: object values must be JSON strings.
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(
                JsonObject(
                    mapOf(
                        "en" to JsonPrimitive("ok"),
                        "fr" to JsonArray(emptyList()),
                    )
                )
            )
        }

        // Contract: object cannot be empty.
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonObject(emptyMap()))
        }
    }
}
