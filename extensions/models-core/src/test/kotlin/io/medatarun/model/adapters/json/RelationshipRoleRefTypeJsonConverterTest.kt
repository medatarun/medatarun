package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefMissingKeyException
import io.medatarun.model.domain.*
import kotlinx.serialization.json.JsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RelationshipRoleRefTypeJsonConverterTest {

    private val converter = RelationshipRoleRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UUID.fromString("66666666-7777-8888-9999-000000000000")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(RelationshipRoleRef.ById(RelationshipRoleId(id)), idRef)

        // Contract: key requires model, relationship and role parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:model=m1&relationship=r1&role=ro1"))
        val expectedKeyRef = RelationshipRoleRef.ByKey(
            model = ModelKey("m1"),
            relationship = RelationshipKey("r1"),
            role = RelationshipRoleKey("ro1"),
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
