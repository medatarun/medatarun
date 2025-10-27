package io.medatarun.ext.modeljson

import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelTypeId
import io.medatarun.model.model.ModelVersion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

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
        assertEquals(modelRead.id, ModelId("example"))
        assertEquals(modelRead.version, ModelVersion("1.0.0"))
        assertEquals(modelRead.entityDefs.size, 2)


        val contactEntity = modelRead.entityDefs[0]
        assertEquals(contactEntity.id, EntityDefId("contact"))
        assertEquals(contactEntity.name?.name, "Contact")
        assertEquals(AttributeDefId("name"), contactEntity.identifierAttributeDefId)

        val companyEntity = modelRead.entityDefs[1]
        assertEquals(companyEntity.id, EntityDefId("company"))
        assertEquals(AttributeDefId("name"), companyEntity.identifierAttributeDefId)
        assertEquals(companyEntity.name?.name, "Company")
        assertEquals(companyEntity.name?.get("fr"), "Entreprise")
        assertEquals(companyEntity.name?.get("de"), "Company")

        assertEquals(companyEntity.countAttributeDefs(), 3)

        val companyName = companyEntity.getAttributeDef(AttributeDefId("name"))
        assertEquals(companyName.id, AttributeDefId("name"))
        assertEquals(companyName.name?.name, "Name")
        assertEquals(companyName.description, null)
        assertEquals(companyName.optional, false)
        assertEquals(companyName.type, ModelTypeId("String"))

        val companyProfileUrl = companyEntity.getAttributeDef(AttributeDefId("profile_url"))
        assertEquals(companyProfileUrl.id, AttributeDefId("profile_url"))
        assertEquals(companyProfileUrl.name?.name, "Profile URL")
        assertEquals(companyProfileUrl.description?.name, "Website URL")
        assertEquals(companyProfileUrl.optional, true)
        assertEquals(companyProfileUrl.type, ModelTypeId("String"))

        val companyInfos = companyEntity.getAttributeDef(AttributeDefId("informations"))
        assertEquals(companyInfos.id, AttributeDefId("informations"))
        assertEquals(companyInfos.name?.name, "Informations")
        assertEquals(companyInfos.description?.name, "La description est au format Markdown et doit provenir de leur site internet !")
        assertEquals(companyInfos.optional, true)
        assertEquals(companyInfos.type, ModelTypeId("Markdown"))
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
        val modelWrite = instance.toJson(modelRead)
        val src = normalizeJson(sampleModelJson)
        val dest = normalizeJson(modelWrite)
        assertEquals(src, dest)

    }

    fun normalizeJson(str: String): String {
        val parser = Json { prettyPrint = true }
        val element = parser.parseToJsonElement(str)
        return parser.encodeToString(JsonElement.serializer(), element)
    }
}

