package io.medatarun.ext.modeljson

import io.medatarun.model.domain.*
import kotlinx.serialization.json.*
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ModelJsonConverterTest {

    private val instance = ModelJsonConverter(prettyPrint = true)

    /**
     * Validates the deserialization of a JSON model into a model object and verifies the properties
     * of the deserialized object.
     *
     * This test ensures that everything is in place (meaning in correct objects when deserialized)
     * but doesn't test all edge cases.
     */
    @Test
    fun surface_test() {
        val modelRead = instance.fromJson(sampleModelJson)
        assertEquals(modelRead.id, ModelKey("example"))
        assertEquals(modelRead.version, ModelVersion("1.0.0"))
        assertEquals(modelRead.entityDefs.size, 2)


        val contactEntity = modelRead.entityDefs[0]
        assertEquals(contactEntity.id, EntityKey("contact"))
        assertEquals(contactEntity.name?.name, "Contact")
        assertEquals(AttributeKey("name"), contactEntity.identifierAttributeKey)

        val companyEntity = modelRead.entityDefs[1]
        assertEquals(companyEntity.id, EntityKey("company"))
        assertEquals(AttributeKey("name"), companyEntity.identifierAttributeKey)
        assertEquals(companyEntity.name?.name, "Company")
        assertEquals(companyEntity.name?.get("fr"), "Entreprise")
        assertEquals(companyEntity.name?.get("de"), "Company")

        assertEquals(companyEntity.countAttributeDefs(), 3)

        val companyName = companyEntity.getAttributeDef(AttributeKey("name"))
        assertEquals(companyName.id, AttributeKey("name"))
        assertEquals(companyName.name?.name, "Name")
        assertEquals(companyName.description, null)
        assertEquals(companyName.optional, false)
        assertEquals(companyName.type, TypeKey("String"))

        val companyProfileUrl = companyEntity.getAttributeDef(AttributeKey("profile_url"))
        assertEquals(companyProfileUrl.id, AttributeKey("profile_url"))
        assertEquals(companyProfileUrl.name?.name, "Profile URL")
        assertEquals(companyProfileUrl.description?.name, "Website URL")
        assertEquals(companyProfileUrl.optional, true)
        assertEquals(companyProfileUrl.type, TypeKey("String"))

        val companyInfos = companyEntity.getAttributeDef(AttributeKey("informations"))
        assertEquals(companyInfos.id, AttributeKey("informations"))
        assertEquals(companyInfos.name?.name, "Informations")
        assertEquals(
            companyInfos.description?.name,
            "La description est au format Markdown et doit provenir de leur site internet !"
        )
        assertEquals(companyInfos.optional, true)
        assertEquals(companyInfos.type, TypeKey("Markdown"))
    }

    /**
     * Tests whether a JSON model, serialized and deserialized using the `ModelJsonConverter`,
     * remains unchanged except for formatting differences. This ensures that the process of
     * serialization followed by deserialization of a model incurs the minimal necessary
     * changes to its structure.
     *
     * This test procedure involves:
     * - Deserializing a sample JSON string into a model object using `fromJson`.
     * - Serializing the resulting model object back into a JSON string using `toJson`.
     * - Normalizing both input and output JSON strings to ensure consistent formatting
     *   for comparison.
     * - Asserting that the normalized input and output JSON strings are identical.
     */
    @Test
    fun writes_as_read_with_minimum_changes() {
        val modelRead = instance.fromJson(sampleModelJson)
        val modelWrite = instance.toJsonString(modelRead)
        val src = normalizeJson(sampleModelJson)
        val dest = normalizeJson(modelWrite)
        assertEquals(src, dest)
    }

    fun createJsonForOriginTest(origin: JsonElement? = null): JsonObject {
        return buildJsonObject {
            put("id", "exemple")
            put("version", "1.0.0")
            put($$"$schema", ModelJsonSchemas.initial)
            putJsonArray("types") { addJsonObject { put("id", "string") } }
            putJsonArray("entities") {
                addJsonObject {
                    put("id", "contact")
                    put("name", "Contact")
                    put("identifierAttribute", "id")
                    when (origin) {
                        null -> {}
                        is JsonNull -> put("origin", JsonNull)
                        else -> put("origin", origin)
                    }
                    putJsonArray("attributes") {
                        addJsonObject {
                            put("id", "id")
                            put("name", "Identifier")
                            put("type", "String")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `entity origin undefined then manual`() {
        val json = createJsonForOriginTest(null)
        val model = instance.fromJson(json.toString())
        assertEquals(EntityOrigin.Manual, model.findEntityDef(EntityKey("contact")).origin)
    }

    @Test
    fun `entity origin null then manual`() {
        val json = createJsonForOriginTest(JsonNull)
        val model = instance.fromJson(json.toString())
        assertEquals(EntityOrigin.Manual, model.findEntityDef(EntityKey("contact")).origin)
    }

    @Test
    fun `entity origin uri then uri`() {
        val url = "https://www.example.local/schema.json"
        val json = createJsonForOriginTest(JsonPrimitive(url))
        val model = instance.fromJson(json.toString())
        assertEquals(EntityOrigin.Uri(URI(url)), model.findEntityDef(EntityKey("contact")).origin)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        val originRead = modelRead.findEntityDef(EntityKey("contact")).origin
        assertEquals(url, (originRead as EntityOrigin.Uri).uri.toString())
    }

    fun createJsonForDocumentationHomeTest(modelDocHome: JsonElement?, entityDocHome: JsonElement?): JsonObject {
        return buildJsonObject {
            put("id", "exemple")
            put("version", "1.0.0")
            put($$"$schema", ModelJsonSchemas.initial)
            if (modelDocHome != null) put("documentationHome", modelDocHome)
            putJsonArray("types") { addJsonObject { put("id", "string") } }
            putJsonArray("entities") {
                addJsonObject {
                    put("id", "contact")
                    put("name", "Contact")
                    put("identifierAttribute", "id")
                    if (entityDocHome != null) put("documentationHome", entityDocHome)
                    putJsonArray("attributes") {
                        addJsonObject {
                            put("id", "id")
                            put("name", "Identifier")
                            put("type", "String")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `model and entity documentation home undefined`() {
        val json = createJsonForDocumentationHomeTest(null, null)
        val model = instance.fromJson(json.toString())
        assertEquals(null, model.documentationHome)
        assertEquals(null, model.entityDefs.first().documentationHome)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        assertEquals(null, modelRead.documentationHome)
        assertEquals(null, modelRead.entityDefs.first().documentationHome)
    }

    @Test
    fun `model and entity documentation home null`() {
        val json = createJsonForDocumentationHomeTest(JsonNull, JsonNull)
        val model = instance.fromJson(json.toString())
        assertEquals(null, model.documentationHome)
        assertEquals(null, model.entityDefs.first().documentationHome)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        assertEquals(null, modelRead.documentationHome)
        assertEquals(null, modelRead.entityDefs.first().documentationHome)
    }

    @Test
    fun `model and entity documentation home defined`() {
        val modelDocHome = "https://localhost/model"
        val entityDocHome = "https://localhost/entity"
        val json = createJsonForDocumentationHomeTest(JsonPrimitive(modelDocHome), JsonPrimitive(entityDocHome))
        val model = instance.fromJson(json.toString())
        assertEquals(URI(modelDocHome).toURL(), model.documentationHome)
        assertEquals(URI(entityDocHome).toURL(), model.entityDefs.first().documentationHome)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        assertEquals(URI(modelDocHome).toURL(), modelRead.documentationHome)
        assertEquals(URI(entityDocHome).toURL(), modelRead.entityDefs.first().documentationHome)
    }

    fun createJsonForHashtagsTest(modelHashtags: JsonElement?, entityHashtags: JsonElement?): JsonObject {
        return buildJsonObject {
            put("id", "exemple")
            put("version", "1.0.0")
            put($$"$schema", ModelJsonSchemas.initial)
            if (modelHashtags != null) put("hashtags", modelHashtags)
            putJsonArray("types") { addJsonObject { put("id", "string") } }
            putJsonArray("entities") {
                addJsonObject {
                    put("id", "contact")
                    put("name", "Contact")
                    put("identifierAttribute", "id")
                    if (entityHashtags != null) put("hashtags", entityHashtags)
                    putJsonArray("attributes") {
                        addJsonObject {
                            put("id", "id")
                            put("name", "Identifier")
                            put("type", "String")
                        }
                    }
                }
            }
        }
    }


    @Test
    fun `model and entity hashtags undefined`() {
        val json = createJsonForHashtagsTest(null, null)
        val model = instance.fromJson(json.toString())
        assertTrue(model.hashtags.isEmpty())
        assertTrue(model.entityDefs.first().hashtags.isEmpty())
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)
        assertTrue(modelRead.hashtags.isEmpty())
        assertTrue(modelRead.entityDefs.first().hashtags.isEmpty())
    }

    @Test
    fun `model and entity hashtags null`() {
        val json = createJsonForHashtagsTest(JsonNull, JsonNull)
        val model = instance.fromJson(json.toString())
        assertTrue(model.hashtags.isEmpty())
        assertTrue(model.entityDefs.first().hashtags.isEmpty())
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)
        assertTrue(modelRead.hashtags.isEmpty())
        assertTrue(modelRead.entityDefs.first().hashtags.isEmpty())
    }

    @Test
    fun `model and entity hashtags empty list`() {
        val json = createJsonForHashtagsTest(buildJsonArray {}, buildJsonArray { })
        val model = instance.fromJson(json.toString())
        assertTrue(model.hashtags.isEmpty())
        assertTrue(model.entityDefs.first().hashtags.isEmpty())
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)
        assertTrue(modelRead.hashtags.isEmpty())
        assertTrue(modelRead.entityDefs.first().hashtags.isEmpty())
    }

    @Test
    fun `model and entity hashtags home defined`() {
        val modelDocHome = listOf("tag1", "tag2")
        val entityDocHome = listOf("tag3", "tag4")
        val json = createJsonForHashtagsTest(
            JsonArray(modelDocHome.map { JsonPrimitive(it) }),
            JsonArray(entityDocHome.map { JsonPrimitive(it) })
        )
        val model = instance.fromJson(json.toString())


        assertContains(model.hashtags, Hashtag("tag1"))
        assertContains(model.hashtags, Hashtag("tag2"))
        assertContains(model.entityDefs.first().hashtags, Hashtag("tag3"))
        assertContains(model.entityDefs.first().hashtags, Hashtag("tag4"))

        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)

        assertContains(modelRead.hashtags, Hashtag("tag1"))
        assertContains(modelRead.hashtags, Hashtag("tag2"))
        assertContains(modelRead.entityDefs.first().hashtags, Hashtag("tag3"))
        assertContains(modelRead.entityDefs.first().hashtags, Hashtag("tag4"))
    }

    fun normalizeJson(str: String): String {
        val parser = Json { prettyPrint = true }
        val element = parser.parseToJsonElement(str)
        return parser.encodeToString(JsonElement.serializer(), element)
    }
}

