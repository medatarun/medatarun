package io.medatarun.model.adapters.json

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.adapters.TypeJsonInvalidRefException
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityAttributeRef
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityAttributeRefTypeJsonConverterTest {

    private val converter = EntityAttributeRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UuidUtils.fromString("11111111-2222-3333-4444-555555555555")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(EntityAttributeRef.ById(AttributeId(id)), idRef)

        // Contract: key requires model, entity and attribute parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:a1"))
        val expectedKeyRef = EntityAttributeRef.ByKey(AttributeKey("a1"))
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            converter.deserialize(JsonPrimitive("key:"))
        }
    }
}
