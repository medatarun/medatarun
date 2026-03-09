package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.internal.ModelJsonConverter
import io.medatarun.model.domain.*
import kotlinx.serialization.json.*
import java.net.URI
import kotlin.test.*

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


        // Copied from Json
        val typeIdString = TypeId.fromString("019be5cd-2ce6-7c51-b4ec-43aa4517b56b")
        val typeIdMarkdown = TypeId.fromString("019be5cd-499b-7663-ac33-e28c419a5bba")
        val contactNameAttributeId = AttributeId.fromString("019be5cd-e3e3-715a-9de9-4aa368a2401c")
        val companyNameAttributeId = AttributeId.fromString("019be5cf-142c-737d-a1c2-3434cdb13912")

        val modelRead = instance.fromJson(sampleModelJson)
        assertEquals(modelRead.key, ModelKey("example"))
        assertEquals(modelRead.version, ModelVersion("1.0.0"))
        assertEquals(ModelAuthority.SYSTEM, modelRead.authority)
        assertEquals(modelRead.entities.size, 2)
        assertEquals(
            setOf(
                "11111111-1111-1111-1111-111111111111",
                "22222222-2222-2222-2222-222222222222",
                "33333333-3333-3333-3333-333333333333",
                "44444444-4444-4444-4444-444444444444"
            ),
            modelRead.tags.map { it.value.toString() }.toSet()
        )

        val contactEntityRef = EntityRef.ByKey(EntityKey("contact"))
        val companyRef = EntityRef.ByKey(EntityKey("company"))

        val contactEntity = modelRead.findEntity(contactEntityRef)
        assertEquals(contactEntity.key, EntityKey("contact"))
        assertEquals(contactEntity.name?.name, "Contact")
        assertEquals(contactNameAttributeId, contactEntity.identifierAttributeId)
        assertEquals(
            setOf(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1",
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2"
            ),
            contactEntity.tags.map { it.value.toString() }.toSet()
        )

        val companyEntity = modelRead.findEntity(companyRef)
        assertEquals(companyEntity.key, EntityKey("company"))
        assertEquals(companyNameAttributeId, companyEntity.identifierAttributeId)
        assertEquals(companyEntity.name?.name, "Company")
        assertEquals(companyEntity.name?.get("fr"), "Entreprise")
        assertEquals(companyEntity.name?.get("de"), "Company")
        assertEquals(
            setOf(
                "cccccccc-cccc-cccc-cccc-ccccccccccc1",
                "cccccccc-cccc-cccc-cccc-ccccccccccc2"
            ),
            companyEntity.tags.map { it.value.toString() }.toSet()
        )

        val companyNameRef = EntityAttributeRef.ByKey(AttributeKey("name"))
        val companyName = modelRead.findEntityAttributeOptional(companyRef,companyNameRef)
        assertNotNull(companyName)
        assertEquals(companyName.key, AttributeKey("name"))
        assertEquals(companyName.name?.name, "Name")
        assertEquals(companyName.description, null)
        assertEquals(companyName.optional, false)
        assertEquals(companyName.typeId, typeIdString)
        assertTrue(companyName.tags.isEmpty())

        val companyProfileUrlRef = EntityAttributeRef.ByKey(AttributeKey("profile_url"))
        val companyProfileUrl = modelRead.findEntityAttributeOptional(companyRef, companyProfileUrlRef)
        assertNotNull(companyProfileUrl)
        assertEquals(companyProfileUrl.key, AttributeKey("profile_url"))
        assertEquals(companyProfileUrl.name?.name, "Profile URL")
        assertEquals(companyProfileUrl.description?.name, "Website URL")
        assertEquals(companyProfileUrl.optional, true)
        assertEquals(companyProfileUrl.typeId, typeIdString)

        val companyInfosRef = EntityAttributeRef.ByKey(AttributeKey("informations"))
        val companyInfos = modelRead.findEntityAttributeOptional(companyRef, companyInfosRef)
        assertNotNull(companyInfos)
        assertEquals(companyInfos.key, AttributeKey("informations"))
        assertEquals(companyInfos.name?.name, "Informations")
        assertEquals(
            companyInfos.description?.name,
            "La description est au format Markdown et doit provenir de leur site internet !"
        )
        assertEquals(companyInfos.optional, true)
        assertEquals(companyInfos.typeId, typeIdMarkdown)

        val contactName = modelRead.findEntityAttributeOptional(contactEntityRef, EntityAttributeRef.ByKey(AttributeKey("name")))
        assertNotNull(contactName)
        assertEquals(
            setOf("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1"),
            contactName.tags.map { it.value.toString() }.toSet()
        )
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
            put("id", ModelId.generate().value.toString())
            put("key", "exemple")
            put("version", "1.0.0")
            put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0))
            putJsonArray("types") { addJsonObject {
                put("id", TypeId.generate().value.toString())
                put("key", "String") }
            }
            putJsonArray("entities") {
                addJsonObject {
                    put("id", EntityId.generate().value.toString())
                    put("key", "contact")
                    put("name", "Contact")
                    put("identifierAttribute", "id")
                    when (origin) {
                        null -> {}
                        is JsonNull -> put("origin", JsonNull)
                        else -> put("origin", origin)
                    }
                    putJsonArray("attributes") {
                        addJsonObject {
                            put("id", AttributeId.generate().value.toString())
                            put("key", "id")
                            put("name", "Identifier")
                            put("type", "String")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `model authority undefined then system`() {
        val json = createJsonForOriginTest(null)
        val model = instance.fromJson(json.toString())
        assertEquals(ModelAuthority.SYSTEM, model.authority)
    }

    @Test
    fun `model authority canonical then canonical`() {
        val json = buildJsonObject {
            put("id", ModelId.generate().value.toString())
            put("key", "exemple")
            put("version", "1.0.0")
            put($$"$schema", ModelJsonSchemas.forVersion(ModelJsonSchemas.v_2_0))
            put("authority", "canonical")
            putJsonArray("types") {
                addJsonObject {
                    put("id", TypeId.generate().value.toString())
                    put("key", "String")
                }
            }
            putJsonArray("entities") {
                addJsonObject {
                    put("id", EntityId.generate().value.toString())
                    put("key", "contact")
                    put("name", "Contact")
                    put("identifierAttribute", "id")
                    putJsonArray("attributes") {
                        addJsonObject {
                            put("id", AttributeId.generate().value.toString())
                            put("key", "id")
                            put("name", "Identifier")
                            put("type", "String")
                        }
                    }
                }
            }
        }
        val model = instance.fromJson(json.toString())
        assertEquals(ModelAuthority.CANONICAL, model.authority)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)
        assertEquals(ModelAuthority.CANONICAL, modelRead.authority)
    }

    @Test
    fun `entity origin undefined then manual`() {
        val json = createJsonForOriginTest(null)
        val model = instance.fromJson(json.toString())
        assertEquals(EntityOrigin.Manual, model.findEntityOptional(EntityKey("contact"))?.origin)
    }

    @Test
    fun `entity origin null then manual`() {
        val json = createJsonForOriginTest(JsonNull)
        val model = instance.fromJson(json.toString())
        assertEquals(EntityOrigin.Manual, model.findEntityOptional(EntityKey("contact"))?.origin)
    }

    @Test
    fun `entity origin uri then uri`() {
        val url = "https://www.example.local/schema.json"
        val json = createJsonForOriginTest(JsonPrimitive(url))
        val model = instance.fromJson(json.toString())
        assertEquals(EntityOrigin.Uri(URI(url)), model.findEntityOptional(EntityKey("contact"))?.origin)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        val originRead = modelRead.findEntityOptional(EntityKey("contact"))?.origin
        assertEquals(url, (originRead as EntityOrigin.Uri).uri.toString())
    }

    fun createJsonForDocumentationHomeTest(modelDocHome: JsonElement?, entityDocHome: JsonElement?): JsonObject {
        return buildJsonObject {
            put("id", ModelId.generate().value.toString())
            put("key", "exemple")
            put("version", "1.0.0")
            put($$"$schema", ModelJsonSchemas.v_1_0)
            if (modelDocHome != null) put("documentationHome", modelDocHome)
            putJsonArray("types") { addJsonObject {
                put("id", TypeId.generate().value.toString())
                put("key", "String") }
            }
            putJsonArray("entities") {
                addJsonObject {
                    put("id", EntityId.generate().value.toString())
                    put("key", "contact")
                    put("name", "Contact")
                    put("identifierAttribute", "id")
                    if (entityDocHome != null) put("documentationHome", entityDocHome)
                    putJsonArray("attributes") {
                        addJsonObject {
                            put("id", AttributeId.generate().value.toString())
                            put("key", "id")
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
        assertEquals(null, model.entities.first().documentationHome)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        assertEquals(null, modelRead.documentationHome)
        assertEquals(null, modelRead.entities.first().documentationHome)
    }

    @Test
    fun `model and entity documentation home null`() {
        val json = createJsonForDocumentationHomeTest(JsonNull, JsonNull)
        val model = instance.fromJson(json.toString())
        assertEquals(null, model.documentationHome)
        assertEquals(null, model.entities.first().documentationHome)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        assertEquals(null, modelRead.documentationHome)
        assertEquals(null, modelRead.entities.first().documentationHome)
    }

    @Test
    fun `model and entity documentation home defined`() {
        val modelDocHome = "https://localhost/model"
        val entityDocHome = "https://localhost/entity"
        val json = createJsonForDocumentationHomeTest(JsonPrimitive(modelDocHome), JsonPrimitive(entityDocHome))
        val model = instance.fromJson(json.toString())
        assertEquals(URI(modelDocHome).toURL(), model.documentationHome)
        assertEquals(URI(entityDocHome).toURL(), model.entities.first().documentationHome)
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten.toString())
        assertEquals(URI(modelDocHome).toURL(), modelRead.documentationHome)
        assertEquals(URI(entityDocHome).toURL(), modelRead.entities.first().documentationHome)
    }

    fun createJsonForTagsTest(modelTags: JsonElement?, entityTags: JsonElement?): JsonObject {
        return buildJsonObject {
            put("id", ModelId.generate().value.toString())
            put("key", "exemple")
            put("version", "1.0.0")
            put($$"$schema", ModelJsonSchemas.v_1_0)
            if (modelTags != null) put("tags", modelTags)
            putJsonArray("types") {
                addJsonObject {
                    put("id", TypeId.generate().value.toString())
                    put("key", "string")
                }
            }
            putJsonArray("entities") {
                addJsonObject {
                    put("id", EntityId.generate().value.toString())
                    put("key", "contact")
                    put("name", "Contact")
                    put("identifierAttribute", "id")
                    if (entityTags != null) put("tags", entityTags)
                    putJsonArray("attributes") {
                        addJsonObject {
                            put("id", AttributeId.generate().value.toString())
                            put("key", "id")
                            put("name", "Identifier")
                            put("type", "string")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `model and entity tags undefined`() {
        val json = createJsonForTagsTest(null, null)
        val model = instance.fromJson(json.toString())
        assertTrue(model.tags.isEmpty())
        assertTrue(model.entities.first().tags.isEmpty())
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)
        assertTrue(modelRead.tags.isEmpty())
        assertTrue(modelRead.entities.first().tags.isEmpty())
    }

    @Test
    fun `model and entity tags null`() {
        val json = createJsonForTagsTest(JsonNull, JsonNull)
        val model = instance.fromJson(json.toString())
        assertTrue(model.tags.isEmpty())
        assertTrue(model.entities.first().tags.isEmpty())
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)
        assertTrue(modelRead.tags.isEmpty())
        assertTrue(modelRead.entities.first().tags.isEmpty())
    }

    @Test
    fun `model and entity tags empty list`() {
        val json = createJsonForTagsTest(buildJsonArray {}, buildJsonArray { })
        val model = instance.fromJson(json.toString())
        assertTrue(model.tags.isEmpty())
        assertTrue(model.entities.first().tags.isEmpty())
        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)
        assertTrue(modelRead.tags.isEmpty())
        assertTrue(modelRead.entities.first().tags.isEmpty())
    }

    @Test
    fun `model and entity tags defined`() {
        val modelTags = listOf(
            java.util.UUID.randomUUID().toString(),
            java.util.UUID.randomUUID().toString()
        )
        val entityTags = listOf(
            java.util.UUID.randomUUID().toString(),
            java.util.UUID.randomUUID().toString()
        )
        val json = createJsonForTagsTest(
            JsonArray(modelTags.map { JsonPrimitive(it) }),
            JsonArray(entityTags.map { JsonPrimitive(it) })
        )
        val model = instance.fromJson(json.toString())

        assertEquals(modelTags.toSet(), model.tags.map { it.value.toString() }.toSet())
        assertEquals(entityTags.toSet(), model.entities.first().tags.map { it.value.toString() }.toSet())

        val jsonWritten = instance.toJsonString(model)
        val modelRead = instance.fromJson(jsonWritten)

        assertEquals(modelTags.toSet(), modelRead.tags.map { it.value.toString() }.toSet())
        assertEquals(entityTags.toSet(), modelRead.entities.first().tags.map { it.value.toString() }.toSet())
    }

    fun normalizeJson(str: String): String {
        val parser = Json { prettyPrint = true }
        val element = parser.parseToJsonElement(str)
        return parser.encodeToString(JsonElement.serializer(), element)
    }
}
