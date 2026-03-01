package io.medatarun.type.commons.ref

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.types.TypeJsonConverterIllegalNullException
import io.medatarun.types.TypeJsonJsonObjectExpectedException
import io.medatarun.types.TypeJsonJsonStringExpectedException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RefTypeJsonConvertersTest {

    @Test
    fun `expectingString should accept json strings only`() {
        val result = RefTypeJsonConverters.expectingString(JsonPrimitive("value")) { value ->
            "ok:$value"
        }
        assertEquals("ok:value", result)

        assertFailsWith<TypeJsonConverterIllegalNullException> {
            RefTypeJsonConverters.expectingString(JsonNull) { it }
        }

        assertFailsWith<TypeJsonJsonObjectExpectedException> {
            RefTypeJsonConverters.expectingString(JsonArray(emptyList())) { it }
        }

        assertFailsWith<TypeJsonJsonStringExpectedException> {
            RefTypeJsonConverters.expectingString(JsonObject(emptyMap())) { it }
        }

        assertFailsWith<TypeJsonJsonStringExpectedException> {
            RefTypeJsonConverters.expectingString(JsonPrimitive(12)) { it }
        }
    }

    @Test
    fun `decodeRef should accept id and key references`() {
        val id = UuidUtils.fromString("11111111-2222-3333-4444-555555555555")
        // "id:" path should map to EntityRef.ById and keep the UUID unchanged.
        val idRef = decodeEntityRef(JsonPrimitive("id:$id"))
        assertEquals(SampleRef.ById(SampleId(id)), idRef)

        // "key:" path should map to EntityRef.ByKey and keep raw values for model/entity.
        val keyRef = decodeEntityRef(JsonPrimitive("key:my-entity"))
        val expectedKeyRef = SampleRef.ByKey(
            SampleKey("my-entity"),
        )
        assertEquals(expectedKeyRef, keyRef)

        // Percent-encoded values must be decoded before building keys.
        val encodedKeyRef = decodeEntityRef(JsonPrimitive("key:ent%2F1"))
        val expectedEncodedKeyRef = SampleRef.ByKey(SampleKey("ent/1"))
        assertEquals(expectedEncodedKeyRef, encodedKeyRef)

    }

    @Test
    fun `decodeRef should reject non string json values`() {
        assertFailsWith<TypeJsonConverterIllegalNullException> {
            decodeEntityRef(JsonNull)
        }

        assertFailsWith<TypeJsonJsonObjectExpectedException> {
            decodeEntityRef(JsonArray(emptyList()))
        }

        assertFailsWith<TypeJsonJsonStringExpectedException> {
            decodeEntityRef(JsonObject(emptyMap()))
        }

        assertFailsWith<TypeJsonJsonStringExpectedException> {
            decodeEntityRef(JsonPrimitive(1))
        }
    }

    @Test
    fun `decodeRef should reject invalid ref formats`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            decodeEntityRef(JsonPrimitive("id"))
        }

        assertFailsWith<TypeJsonInvalidRefException> {
            decodeEntityRef(JsonPrimitive("id:"))
        }

        assertFailsWith<TypeJsonInvalidRefException> {
            decodeEntityRef(JsonPrimitive(":123"))
        }

        assertFailsWith<TypeJsonInvalidRefSchemeException> {
            decodeEntityRef(JsonPrimitive("uuid:123"))
        }
    }


    private fun decodeEntityRef(json: JsonElement): SampleRef {
        // Mirror EntityRefTypeJsonConverter's usage of RefTypeJsonConverters.decodeRef.
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                SampleRef.ById(SampleId.fromString(id))
            },
            whenKey = { keyParts -> SampleRef.ByKey(key = SampleKey(keyParts)) }
        )
    }

    @JvmInline
    value class SampleId(val value: UUID) {
        companion object {
            fun fromString(value: String): SampleId {
                return SampleId(UuidUtils.fromString(value))
            }
        }
    }

    @JvmInline
    value class SampleKey(val value: String)

    sealed interface SampleRef {

        fun asString(): String

        data class ById(val id: SampleId) : SampleRef {
            override fun asString(): String {
                return "id:" + id.value
            }
        }

        data class ByKey(val key: SampleKey) : SampleRef {
            override fun asString(): String {
                return "key:" + key.value
            }
        }
    }
}