package io.medatarun.tags.core.adapters.json

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.tags.core.domain.*
import io.medatarun.type.commons.key.KeyStrictInvalidFormatException
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

        val localScopeId = UuidUtils.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
        val freeKeyRef = converter.deserialize(JsonPrimitive("key:vehicle/$localScopeId/Release_2026"))
        assertEquals(
            TagRef.ByKey(
                scopeRef = TagScopeRef.Local(TagScopeType("vehicle"), TagScopeId(localScopeId)),
                groupKey = null,
                key = TagKey("Release_2026")
            ),
            freeKeyRef
        )

        val managedKeyRef = converter.deserialize(JsonPrimitive("key:global/Project_A/Release-2026"))
        assertEquals(
            TagRef.ByKey(scopeRef = TagScopeRef.Global, groupKey = TagGroupKey("Project_A"), key = TagKey("Release-2026")),
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
            converter.deserialize(JsonPrimitive("key:/x/tag"))
        }
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive("key:global/group/"))
        }
        assertFailsWith<TypeJsonConverterBadFormatException> {
            converter.deserialize(JsonPrimitive("key:global/group/tag/extra"))
        }
    }

    @Test
    fun `deserialize should reject invalid characters in tag keys`() {
        assertFailsWith<KeyStrictInvalidFormatException> {
            converter.deserialize(JsonPrimitive("key:global/group/Tag Value"))
        }
    }
}
