package io.medatarun.model.adapters.json

import io.medatarun.model.domain.diff.ModelDiffScope
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModelDiffScopeTypeJsonConverterTest {

    private val converter = ModelDiffScopeTypeJsonConverter()

    @Test
    fun `deserialize should accept model diff scope codes`() {
        assertEquals(ModelDiffScope.STRUCTURAL, converter.deserialize(JsonPrimitive("structural")))
        assertEquals(ModelDiffScope.COMPLETE, converter.deserialize(JsonPrimitive("complete")))
    }

    @Test
    fun `deserialize should reject invalid model diff scope code`() {
        assertFailsWith<TypeJsonConverterBadFormatException> {
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
