package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefMissingKeyException
import io.medatarun.model.domain.*
import kotlinx.serialization.json.JsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RelationshipAttributeRefTypeJsonConverterTest {

    private val converter = RelationshipAttributeRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UUID.fromString("44444444-5555-6666-7777-888888888888")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(RelationshipAttributeRef.ById(AttributeId(id)), idRef)

        // Contract: key requires model, relationship and attribute parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:model=m1&relationship=r1&attribute=a1"))
        val expectedKeyRef = RelationshipAttributeRef.ByKey(
            model = ModelKey("m1"),
            relationship = RelationshipKey("r1"),
            attribute = AttributeKey("a1"),
        )
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key parts`() {
        assertFailsWith<TypeJsonInvalidRefMissingKeyException> {
            converter.deserialize(JsonPrimitive("key:model=m1&relationship=r1"))
        }
    }
}
