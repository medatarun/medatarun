package io.medatarun.model.adapters.json

import io.medatarun.model.domain.ModelAuthority
import io.medatarun.model.domain.ModelAuthorityIllegalCodeException
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModelAuthorityTypeJsonConverterTest {

    private val converter = ModelAuthorityTypeJsonConverter()

    @Test
    fun `deserialize should accept model authority codes`() {
        assertEquals(ModelAuthority.SYSTEM, converter.deserialize(JsonPrimitive("system")))
        assertEquals(ModelAuthority.CANONICAL, converter.deserialize(JsonPrimitive("canonical")))
    }

    @Test
    fun `deserialize should reject invalid model authority code`() {
        assertFailsWith<ModelAuthorityIllegalCodeException> {
            converter.deserialize(JsonPrimitive("invalid"))
        }
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
