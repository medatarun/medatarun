package io.medatarun.model.adapters.json

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.adapters.TypeJsonInvalidRefException
import io.medatarun.model.domain.RelationshipRoleId
import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.domain.RelationshipRoleRef
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RelationshipRoleRefTypeJsonConverterTest {

    private val converter = RelationshipRoleRefTypeJsonConverter()

    @Test
    fun `deserialize should accept id and key formats`() {
        val id = UuidUtils.fromString("66666666-7777-8888-9999-000000000000")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(RelationshipRoleRef.ById(RelationshipRoleId(id)), idRef)

        // Contract: key requires model, relationship and role parts.
        val keyRef = converter.deserialize(JsonPrimitive("key:ro1"))
        val expectedKeyRef = RelationshipRoleRef.ByKey(RelationshipRoleKey("ro1"))
        assertEquals(expectedKeyRef, keyRef)
    }

    @Test
    fun `deserialize should reject missing key`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            converter.deserialize(JsonPrimitive("key:"))
        }
    }
}
