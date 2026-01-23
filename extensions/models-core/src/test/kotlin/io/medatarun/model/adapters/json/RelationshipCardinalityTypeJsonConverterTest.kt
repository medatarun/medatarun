package io.medatarun.model.adapters.json

import io.medatarun.model.domain.RelationshipCardinality
import io.medatarun.model.domain.RelationshipCardinalityIllegalCodeException
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RelationshipCardinalityTypeJsonConverterTest {

    private val converter = RelationshipCardinalityTypeJsonConverter()

    @Test
    fun `deserialize should accept relationship cardinality codes`() {
        assertEquals(RelationshipCardinality.ZeroOrOne, converter.deserialize(JsonPrimitive("zeroOrOne")))
        assertEquals(RelationshipCardinality.One, converter.deserialize(JsonPrimitive("one")))
        assertEquals(RelationshipCardinality.Many, converter.deserialize(JsonPrimitive("many")))
        assertEquals(RelationshipCardinality.Unknown, converter.deserialize(JsonPrimitive("unknown")))
    }

    @Test
    fun `deserialize should reject invalid cardinality code`() {
        assertFailsWith<RelationshipCardinalityIllegalCodeException> {
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
