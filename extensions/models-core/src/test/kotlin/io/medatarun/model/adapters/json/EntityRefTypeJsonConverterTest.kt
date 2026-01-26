package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefException
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityRef
import kotlinx.serialization.json.JsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityRefTypeJsonConverterTest {

    private val converter = EntityRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UUID.fromString("33333333-4444-5555-6666-777777777777")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(EntityRef.ById(EntityId(id)), idRef)

        // Contract: key requires model and entity parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:e1"))
        val expectedKeyRef = EntityRef.ByKey(EntityKey("e1"))
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            converter.deserialize(JsonPrimitive("key:"))
        }
    }
}
