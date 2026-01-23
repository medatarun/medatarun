package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.*
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.ModelKey
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions
import java.util.*
import kotlin.test.Test

class RefTypeJsonConvertersTest {

    @Test
    fun `expectingString should accept json strings only`() {
        val result = RefTypeJsonConverters.expectingString(JsonPrimitive("value")) { value ->
            "ok:$value"
        }
        Assertions.assertEquals("ok:value", result)

        Assertions.assertThrows(TypeJsonConverterIllegalNullException::class.java) {
            RefTypeJsonConverters.expectingString(JsonNull) { it }
        }

        Assertions.assertThrows(TypeJsonJsonObjectExpectedException::class.java) {
            RefTypeJsonConverters.expectingString(JsonArray(emptyList())) { it }
        }

        Assertions.assertThrows(TypeJsonJsonStringExpectedException::class.java) {
            RefTypeJsonConverters.expectingString(JsonObject(emptyMap())) { it }
        }

        Assertions.assertThrows(TypeJsonJsonStringExpectedException::class.java) {
            RefTypeJsonConverters.expectingString(JsonPrimitive(12)) { it }
        }
    }

    @Test
    fun `decodeRef should accept id and key references`() {
        val id = UUID.fromString("11111111-2222-3333-4444-555555555555")
        // "id:" path should map to EntityRef.ById and keep the UUID unchanged.
        val idRef = decodeEntityRef(JsonPrimitive("id:$id"))
        Assertions.assertEquals(EntityRef.ById(EntityId(id)), idRef)

        // "key:" path should map to EntityRef.ByKey and keep raw values for model/entity.
        val keyRef = decodeEntityRef(JsonPrimitive("key:model=my-model&entity=my-entity"))
        val expectedKeyRef = EntityRef.ByKey(
            model = ModelKey("my-model"),
            entity = EntityKey("my-entity"),
        )
        Assertions.assertEquals(expectedKeyRef, keyRef)

        // Percent-encoded values must be decoded before building keys.
        val encodedKeyRef = decodeEntityRef(JsonPrimitive("key:model=my%20model&entity=ent%2F1"))
        val expectedEncodedKeyRef = EntityRef.ByKey(
            model = ModelKey("my model"),
            entity = EntityKey("ent/1"),
        )
        Assertions.assertEquals(expectedEncodedKeyRef, encodedKeyRef)
    }

    @Test
    fun `decodeRef should reject non string json values`() {
        Assertions.assertThrows(TypeJsonConverterIllegalNullException::class.java) {
            decodeEntityRef(JsonNull)
        }

        Assertions.assertThrows(TypeJsonJsonObjectExpectedException::class.java) {
            decodeEntityRef(JsonArray(emptyList()))
        }

        Assertions.assertThrows(TypeJsonJsonStringExpectedException::class.java) {
            decodeEntityRef(JsonObject(emptyMap()))
        }

        Assertions.assertThrows(TypeJsonJsonStringExpectedException::class.java) {
            decodeEntityRef(JsonPrimitive(1))
        }
    }

    @Test
    fun `decodeRef should reject invalid ref formats`() {
        Assertions.assertThrows(TypeJsonInvalidRefException::class.java) {
            decodeEntityRef(JsonPrimitive("id"))
        }

        Assertions.assertThrows(TypeJsonInvalidRefException::class.java) {
            decodeEntityRef(JsonPrimitive("id:"))
        }

        Assertions.assertThrows(TypeJsonInvalidRefException::class.java) {
            decodeEntityRef(JsonPrimitive(":123"))
        }

        Assertions.assertThrows(TypeJsonInvalidRefSchemeException::class.java) {
            decodeEntityRef(JsonPrimitive("uuid:123"))
        }
    }

    @Test
    fun `decodeRef should reject invalid key query segments`() {
        Assertions.assertThrows(TypeJsonInvalidRefQuerySegmentException::class.java) {
            decodeEntityRef(JsonPrimitive("key:model=a&&entity=b"))
        }

        Assertions.assertThrows(TypeJsonInvalidRefQuerySegmentException::class.java) {
            decodeEntityRef(JsonPrimitive("key:=a&entity=b"))
        }

        Assertions.assertThrows(TypeJsonInvalidRefQuerySegmentException::class.java) {
            decodeEntityRef(JsonPrimitive("key:model=a&entity"))
        }
    }

    @Test
    fun `decodeRef should reject duplicate key query params`() {
        Assertions.assertThrows(TypeJsonInvalidRefDuplicateQueryParamException::class.java) {
            decodeEntityRef(JsonPrimitive("key:model=a&model=b&entity=c"))
        }
    }

    @Test
    fun `decodeRef should surface missing required key parts`() {
        Assertions.assertThrows(TypeJsonInvalidRefMissingKeyException::class.java) {
            decodeEntityRef(JsonPrimitive("key:model=a"))
        }

        Assertions.assertThrows(TypeJsonInvalidRefMissingKeyException::class.java) {
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