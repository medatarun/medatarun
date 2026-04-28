package io.medatarun.model.adapters.json

import io.medatarun.model.domain.TextMarkdown
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TextMarkdownTypeJsonConverterTest {

    private val converter = TextMarkdownTypeJsonConverter()

    @Test
    fun `deserialize should accept string and object formats`() {
        val text = converter.deserialize(JsonPrimitive("**hello**"))
        assertEquals(TextMarkdown("**hello**"), text)
    }

    @Test
    fun `deserialize should reject non string inputs`() {
        assertFailsWith<TypeJsonConverterIllegalNullException> {
            converter.deserialize(JsonNull)
        }

        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonArray(emptyList()))
        }

        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive(12))
        }

        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonObject(emptyMap()))
        }
    }

}
