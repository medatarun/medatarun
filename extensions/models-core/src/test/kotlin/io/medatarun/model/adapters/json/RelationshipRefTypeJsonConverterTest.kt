package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefMissingKeyException
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipRef
import kotlinx.serialization.json.JsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RelationshipRefTypeJsonConverterTest {

    private val converter = RelationshipRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UUID.fromString("55555555-6666-7777-8888-999999999999")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(RelationshipRef.ById(RelationshipId(id)), idRef)

        // Contract: key requires model and relationship parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:model=m1&relationship=r1"))
        val expectedKeyRef = RelationshipRef.ByKey(
            model = ModelKey("m1"),
            relationship = RelationshipKey("r1"),
        )
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key parts`() {
        assertFailsWith<TypeJsonInvalidRefMissingKeyException> {
            converter.deserialize(JsonPrimitive("key:model=m1"))
        }
    }
}
