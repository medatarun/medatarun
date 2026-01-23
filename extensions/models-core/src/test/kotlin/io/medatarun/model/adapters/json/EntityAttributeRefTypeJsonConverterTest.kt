package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefMissingKeyException
import io.medatarun.model.domain.*
import kotlinx.serialization.json.JsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityAttributeRefTypeJsonConverterTest {

    private val converter = EntityAttributeRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UUID.fromString("11111111-2222-3333-4444-555555555555")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(EntityAttributeRef.ById(AttributeId(id)), idRef)

        // Contract: key requires model, entity and attribute parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:model=m1&entity=e1&attribute=a1"))
        val expectedKeyRef = EntityAttributeRef.ByKey(
            model = ModelKey("m1"),
            entity = EntityKey("e1"),
            attribute = AttributeKey("a1"),
        )
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key parts`() {
        assertFailsWith<TypeJsonInvalidRefMissingKeyException> {
            converter.deserialize(JsonPrimitive("key:model=m1&entity=e1"))
        }
    }
}
