package io.medatarun.ext.modeljson.v3

import io.medatarun.ext.modeljson.ModelJsonSchemas
import io.medatarun.ext.modeljson.internal.ModelJsonConverter
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelAuthority
import io.medatarun.model.domain.ModelAuthorityIllegalCodeException
import io.medatarun.model.domain.EntityOrigin
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.assertThrows

internal class JsonDeserializerV3Test {

    private val converter = ModelJsonConverter(false)

    // -------------------------------------------------------------------------
    // Model read
    // -------------------------------------------------------------------------

    @Test
    fun `model reads with optional fields absent`() {
        // Why: lock mapping for required model root fields.
        val modelId = Id.generate(::ModelId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "key":"root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical"
            }
        """.trimIndent()

        val modelJson = converter.fromJsonV3(jsonString)
        assertEquals(modelId, modelJson.id.asString())
        assertEquals(ModelKey("root-fields-test"), modelJson.key)
        assertEquals(ModelVersion("1.2.3"), modelJson.version)
        assertEquals(ModelAuthority.CANONICAL, modelJson.authority)
    }

    @Test
    fun `model reads optional documentation home`() {
        // Why: verify documentationHome is read when provided.
        val modelId = Id.generate(::ModelId).asString()
        val docHome = "https://docs.example.org/model"
        val jsonString = """
            {
              "id":"$modelId",
              "key":"root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical",
              "documentationHome":"$docHome"
            }
        """.trimIndent()

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(docHome, modelJson.documentationHome?.toString())
    }

    @Test
    fun `model reads tags`() {
        // Why: verify model-level tags mapping.
        val modelId = Id.generate(::ModelId).asString()
        val tagA = Id.generate(::TagId).asString()
        val tagB = Id.generate(::TagId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "key":"root-fields-test",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical",
              "tags":["$tagA","$tagB"]
            }
        """.trimIndent()

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(listOf(tagA, tagB), modelJson.tags.map { it.asString() })
    }

    @Test
    fun `model fails when required root field is missing`() {
        // Why: protect against incomplete root payload.
        val modelId = Id.generate(::ModelId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "authority":"canonical"
            }
        """.trimIndent()

        val exception = assertThrows<SerializationException>() {
            converter.fromJsonV3(jsonString)
        }

        assertTrue(exception.localizedMessage.contains("'key'"))
    }

    @Test
    fun `model fails when authority is invalid`() {
        // Why: protect model authority enum parsing.
        val modelId = Id.generate(::ModelId).asString()
        val jsonString = """
            {
              "id":"$modelId",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "key": "mykey",
              "authority":"canonical2"
            }
        """.trimIndent()

        assertThrows<ModelAuthorityIllegalCodeException>() {
            converter.fromJsonV3(jsonString)
        }
    }

    private fun buildModelJson(block: String): String {
        val id = Id.generate(::ModelId)
        return """
            {
              "id":"${id.asString()}",
              "${'$'}schema":"${ModelJsonSchemas.forVersion(ModelJsonSchemas.v_3_0)}",
              "version":"1.2.3",
              "key": "mykey",
              "authority":"canonical",
              $block
            }
        """.trimIndent()
    }

    // -------------------------------------------------------------------------
    // Types read
    // -------------------------------------------------------------------------

    @Test
    fun `types read without name and description`() {
        // Why: verify type optional label fields.
        val typeId = Id.generate(::TypeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(1, modelJson.types.size)
        assertEquals(typeId, modelJson.types.first().id.asString())
        assertEquals(TypeKey("text"), modelJson.types.first().key)
        assertNull(modelJson.types.first().name)
        assertNull(modelJson.types.first().description)
    }
    @Test
    fun `types read with name and description`() {
        // Why: verify label fields.
        val typeId = Id.generate(::TypeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text","name":"Text","description":"Text type"}]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)

        assertEquals(1, modelJson.types.size)
        assertEquals(typeId, modelJson.types.first().id.asString())
        assertEquals(TypeKey("text"), modelJson.types.first().key)
        assertEquals("Text", modelJson.types.first().name?.name)
        assertEquals("Text type", modelJson.types.first().description?.name)
    }

    // -------------------------------------------------------------------------
    // Entities read (entities + entity attributes + primary key)
    // -------------------------------------------------------------------------

    @Test
    fun `entities read correctly when optional fields are absent`() {
        // Why: verify entity import succeeds when optional entity fields are absent
        // and still maps required fields (id/key/attributes) correctly.
        val typeId = Id.generate(::TypeId).asString()
        val entityId = Id.generate(::EntityId).asString()
        val attributeId = Id.generate(::AttributeId).asString()
        val jsonString = buildModelJson(
            """
            "types":[{"id":"$typeId","key":"text"}],
            "entities":[
              {
                "id":"$entityId",
                "key":"customer",
                "attributes":[
                  {"id":"$attributeId","key":"code","type":"text"}
                ]
              }
            ]
            """.trimIndent()
        )

        val modelJson = converter.fromJsonV3(jsonString)
        val entity = modelJson.entities.first()
        val attribute = modelJson.attributes.first()

        assertEquals(entityId, entity.id.asString())
        assertEquals("customer", entity.key.value)
        assertEquals(EntityOrigin.Manual, entity.origin)
        assertEquals(null, entity.name)
        assertEquals(null, entity.description)
        assertEquals(null, entity.documentationHome)
        assertEquals(0, entity.tags.size)

        assertEquals(attributeId, attribute.id.asString())
        assertEquals("code", attribute.key.value)
        assertEquals(false, attribute.optional)
        assertEquals(true, attribute.ownedBy(entity.id))
    }

    @Test
    fun `entities read optional fields when present`() {
        // Why: verify entity optional fields mapping.
        TODO("Read JSON with name/description/origin/documentationHome/tags and assert entity fields")
    }

    @Test
    fun `entity attributes read with type key resolution`() {
        // Why: ensure attribute type key resolves to internal type id.
        TODO("Read JSON where attributes use type key and assert resolved typeId")
    }

    @Test
    fun `entity attributes read optional fields when present`() {
        // Why: verify entity attribute optional fields mapping.
        TODO("Read JSON with attribute name/description/optional/tags and assert fields")
    }

    @Test
    fun `entity attributes fail when type key is unknown`() {
        // Why: protect type reference resolution.
        TODO("Read JSON with unknown attribute type key and assert failure")
    }

    @Test
    fun `entity primary keys read participants in order`() {
        // Why: lock participant order for entity primary key.
        TODO("Read JSON with primaryKey and assert participant order")
    }

    // -------------------------------------------------------------------------
    // Relationships read (relationships + relationship attributes)
    // -------------------------------------------------------------------------

    @Test
    fun `relationships read with name description and tags`() {
        // Why: lock relationship root fields mapping.
        TODO("Read JSON and assert relationship id/key/name/description/tags")
    }

    @Test
    fun `relationship roles read with entity key resolution`() {
        // Why: ensure role entity key resolves to internal entity id.
        TODO("Read JSON with roles.entityId as entity key and assert resolved entityId")
    }

    @Test
    fun `relationship roles fail when entity key is unknown`() {
        // Why: protect role entity reference resolution.
        TODO("Read JSON with unknown role entity key and assert failure")
    }

    @Test
    fun `relationship roles fail when cardinality is invalid`() {
        // Why: protect relationship cardinality enum parsing.
        TODO("Read JSON with invalid role cardinality and assert failure")
    }

    @Test
    fun `relationship attributes read`() {
        // Why: verify relationship-owned attributes mapping.
        TODO("Read JSON with relationship attributes and assert owner/type/fields")
    }

    @Test
    fun `relationships read multiple without data leak`() {
        // Why: ensure relationship-scoped data stays isolated.
        TODO("Read JSON with multiple relationships and assert no role/attribute/tag leakage")
    }

    // -------------------------------------------------------------------------
    // Business keys read
    // -------------------------------------------------------------------------

    @Test
    fun `business keys read with all fields`() {
        // Why: lock full business key mapping.
        TODO("Read JSON with full businessKeys payload and assert all fields")
    }

    @Test
    fun `business keys read without optional fields`() {
        // Why: verify business key optional fields when absent.
        TODO("Read JSON with businessKeys missing name/description and assert null fields")
    }

    @Test
    fun `business keys read participants in order`() {
        // Why: lock participant order for business keys.
        TODO("Read JSON with ordered participants and assert order is preserved")
    }

    @Test
    fun `business keys read when field is absent`() {
        // Why: export can omit businessKeys when empty, import must accept it.
        TODO("Read JSON without businessKeys and assert deserialization succeeds")
    }

    @Test
    fun `business keys fail when entity id is unknown`() {
        // Why: protect business key entity reference resolution.
        TODO("Read JSON with unknown business key entity id and assert failure")
    }

    @Test
    fun `business keys fail when participant attribute id is unknown`() {
        // Why: protect business key participant attribute references.
        TODO("Read JSON with unknown participant attribute id and assert failure")
    }
}
