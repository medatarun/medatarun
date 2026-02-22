package io.medatarun.tags.core.adapters.json

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.type.commons.key.KeyStrictInvalidFormatException
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.type.commons.ref.TypeJsonInvalidRefException
import io.medatarun.types.TypeJsonConverterBadFormatException
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TagRefJsonConverterTest {

    private val converter = TagRefJsonConverter()

    @Test
    fun `deserialize should accept id free key and managed key formats`() {
        val id = UuidUtils.fromString("55555555-6666-7777-8888-999999999999")
        val idRef = converter.deserialize(JsonPrimitive("id:$id"))
        assertEquals(TagRef.ById(TagId(id)), idRef)

        val freeKeyRef = converter.deserialize(JsonPrimitive("key:Release_2026"))
        assertEquals(TagRef.ByKey(groupKey = null, key = TagKey("Release_2026")), freeKeyRef)

        val managedKeyRef = converter.deserialize(JsonPrimitive("key:Project_A/Release-2026"))
        assertEquals(
            TagRef.ByKey(groupKey = TagGroupKey("Project_A"), key = TagKey("Release-2026")),
            managedKeyRef
        )
    }

    @Test
    fun `deserialize should reject missing key`() {
        assertFailsWith<TypeJsonInvalidRefException> {
            converter.deserialize(JsonPrimitive("key:"))
        }
    }

    @Test
    fun `deserialize should reject invalid slash placement or count in key refs`() {
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive("key:/tag"))
        }
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive("key:group/"))
        }
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive("key:group/tag/extra"))
        }
    }

    @Test
    fun `deserialize should reject invalid characters in tag keys`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            converter.deserialize(JsonPrimitive("key:Tag Value"))
        }
    }
}
