package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.*
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.ModelKey
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*
import java.util.*
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
        val id = UUID.fromString("11111111-2222-3333-4444-555555555555")
        // "id:" path should map to EntityRef.ById and keep the UUID unchanged.
        val idRef = decodeEntityRef(JsonPrimitive("id:$id"))
        assertEquals(EntityRef.ById(EntityId(id)), idRef)

        // "key:" path should map to EntityRef.ByKey and keep raw values for model/entity.
        val keyRef = decodeEntityRef(JsonPrimitive("key:model=my-model&entity=my-entity"))
        val expectedKeyRef = EntityRef.ByKey(
            model = ModelKey("my-model"),
            entity = EntityKey("my-entity"),
        )
        assertEquals(expectedKeyRef, keyRef)

        // Percent-encoded values must be decoded before building keys.
        val encodedKeyRef = decodeEntityRef(JsonPrimitive("key:model=my%20model&entity=ent%2F1"))
        val expectedEncodedKeyRef = EntityRef.ByKey(
            model = ModelKey("my model"),
            entity = EntityKey("ent/1"),
        )
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

    @Test
    fun `decodeRef should reject invalid key query segments`() {
        assertFailsWith<TypeJsonInvalidRefQuerySegmentException> {
            decodeEntityRef(JsonPrimitive("key:model=a&&entity=b"))
        }

        assertFailsWith<TypeJsonInvalidRefQuerySegmentException> {
            decodeEntityRef(JsonPrimitive("key:=a&entity=b"))
        }

        assertFailsWith<TypeJsonInvalidRefQuerySegmentException> {
            decodeEntityRef(JsonPrimitive("key:model=a&entity"))
        }
    }

    @Test
    fun `decodeRef should reject duplicate key query params`() {
        assertFailsWith<TypeJsonInvalidRefDuplicateQueryParamException> {
            decodeEntityRef(JsonPrimitive("key:model=a&model=b&entity=c"))
        }
    }

    @Test
    fun `decodeRef should surface missing required key parts`() {
        assertFailsWith<TypeJsonInvalidRefMissingKeyException> {
            decodeEntityRef(JsonPrimitive("key:model=a"))
        }

        assertFailsWith<TypeJsonInvalidRefMissingKeyException> {
            decodeEntityRef(JsonPrimitive("key:entity=b"))
        }
    }

    private fun decodeEntityRef(json: JsonElement): EntityRef {
        // Mirror EntityRefTypeJsonConverter's usage of RefTypeJsonConverters.decodeRef.
        return RefTypeJsonConverters.decodeRef(
            json,
            whenId = { id ->
                EntityRef.ById(EntityId(UUID.fromString(id)))
            },
            whenKey = { keyParts ->
                EntityRef.ByKey(
                    model = ModelKey(keyParts.required("model")),
                    entity = EntityKey(keyParts.required("entity")),
                )
            }
        )
    }
}
