package io.medatarun.model.adapters.json

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.adapters.TypeJsonInvalidRefException
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.RelationshipAttributeRef
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RelationshipAttributeRefTypeJsonConverterTest {

    private val converter = RelationshipAttributeRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UuidUtils.fromString("44444444-5555-6666-7777-888888888888")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(RelationshipAttributeRef.ById(AttributeId(id)), idRef)

        // Contract: key requires model, relationship and attribute parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:a1"))
        val expectedKeyRef = RelationshipAttributeRef.ByKey(key = AttributeKey("a1"))
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            converter.deserialize(JsonPrimitive("key:"))
        }
    }
}
