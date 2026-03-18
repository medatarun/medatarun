package io.medatarun.model.adapters.json

import io.medatarun.model.domain.ModelVersion
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModelVersionTypeJsonConverterTest {

    private val converter = ModelVersionTypeJsonConverter()

    @Test
    fun `deserialize should accept valid model version`() {
        assertEquals(ModelVersion("1.2.3-alpha.1"), converter.deserialize(JsonPrimitive("1.2.3-alpha.1")))
    }

    @Test
    fun `deserialize should reject invalid model version value`() {
        val exception = assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive("1.2.3+build.1"))
        }

        assertEquals("Model version invalid.", exception.message)
    }

    @Test
    fun `deserialize should reject null`() {
        assertFailsWith<TypeJsonConverterIllegalNullException> {
            converter.deserialize(JsonNull)
        }
    }

    @Test
    fun `deserialize should reject non string inputs`() {
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonArray(emptyList()))
        }

        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonObject(emptyMap()))
        }
    }
}
