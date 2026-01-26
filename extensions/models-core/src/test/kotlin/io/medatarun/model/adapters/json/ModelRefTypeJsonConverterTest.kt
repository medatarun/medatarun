package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefException
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelRef
import kotlinx.serialization.json.JsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModelRefTypeJsonConverterTest {

    private val converter = ModelRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UUID.fromString("22222222-3333-4444-5555-666666666666")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(ModelRef.ById(ModelId(id)), idRef)

        // Contract: key requires model part only.
        val keyRef = converter.deserialize(JsonPrimitive("key:m1"))
        val expectedKeyRef = ModelRef.ByKey(ModelKey("m1"))
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            converter.deserialize(JsonPrimitive("key:"))
        }
    }
}
