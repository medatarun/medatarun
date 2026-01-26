package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefException
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeRef
import kotlinx.serialization.json.JsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TypeRefTypeJsonConverterTest {

    private val converter = TypeRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UUID.fromString("77777777-8888-9999-0000-111111111111")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(TypeRef.ById(TypeId(id)), idRef)

        // Contract: key requires model and type parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:t1"))
        val expectedKeyRef = TypeRef.ByKey(TypeKey("t1"))
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            converter.deserialize(JsonPrimitive("key:"))
        }
    }
}
