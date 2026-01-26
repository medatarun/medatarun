package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModelInMemoryTest {


    @Test
    fun `model contains person and company entities`() {
        val model = createModel()
        val entityIds = model.entityDefs.map { it.key }.toSet()
        assertEquals(setOf(EntityKey("person"), EntityKey("company")), entityIds)

        val person = model.entityDefs.first { it.key == EntityKey("person") }
        assertEquals(5, person.countAttributeDefs())

        val attrInfos = model.findEntityAttributeOptional(
            EntityRef.ByKey(EntityKey("person")),
            EntityAttributeRef.ByKey(AttributeKey("infos"))
        )
        assertNotNull(attrInfos)
        assertEquals(TypeKey("Markdown"), attrInfos.type)

        val attrLocation = model.findEntityAttributeOptional(
            EntityRef.ByKey(EntityKey("company")),
            EntityAttributeRef.ByKey(AttributeKey("location"))
        )

        assertNotNull(attrLocation)
        assertTrue(attrLocation.optional)

        val attrWebsite = model.findEntityAttributeOptional(
            EntityRef.ByKey(EntityKey("company")),
            EntityAttributeRef.ByKey(AttributeKey("website"))
        )

        assertNotNull(attrWebsite)
        assertTrue(attrWebsite.optional)
    }

    private fun createModel(): ModelInMemory {

        val typeString = ModelTypeInMemory.of("String")
        val typeMarkdown = ModelTypeInMemory.of("Markdown")

        val personEntity = EntityDefInMemory.builder(
            key = EntityKey("person"),
            identifierAttributeKey = AttributeKey("id"),
        ) {
            name = LocalizedTextNotLocalized("Person")
            addAttribute(
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = typeString.key,
                    optional = false,
                    hashtags = emptyList()
                ),
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("firstName"),
                    name = LocalizedTextNotLocalized("First Name"),
                    description = null,
                    type = typeString.key,
                    optional = false,
                    hashtags = emptyList()
                ),
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("lastName"),
                    name = LocalizedTextNotLocalized("Last Name"),
                    description = null,
                    type = typeString.key,
                    optional = false,
                    hashtags = emptyList()
                ),
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("phoneNumber"),
                    name = LocalizedTextNotLocalized("Phone Number"),
                    description = null,
                    type = typeString.key,
                    optional = false,
                    hashtags = emptyList()
                ),
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("infos"),
                    name = LocalizedTextNotLocalized("Infos"),
                    description = null,
                    type = typeMarkdown.key,
                    optional = false,
                    hashtags = emptyList()
                )
            )
        }

        val companyEntity = EntityDefInMemory.builder(
            key = EntityKey("company"),
            identifierAttributeKey = AttributeKey("id"),
        ) {
            name = LocalizedTextNotLocalized("Company")
            addAttribute(
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = typeString.key,
                    optional = false,
                    hashtags = emptyList()
                ),
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("name"),
                    name = LocalizedTextNotLocalized("Name"),
                    description = null,
                    type = typeString.key,
                    optional = false,
                    hashtags = emptyList()
                ),
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("location"),
                    name = LocalizedTextNotLocalized("Location"),
                    description = null,
                    type = typeString.key,
                    optional = true,
                    hashtags = emptyList()
                ),
                AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("website"),
                    name = LocalizedTextNotLocalized("Website"),
                    description = null,
                    type = typeString.key,
                    optional = true,
                    hashtags = emptyList()
                )
            )

        }

        return ModelInMemory(
            id = ModelId.generate(),
            key = ModelKey("test-model"),
            name = LocalizedTextNotLocalized("Test Model"),
            description = null,
            version = ModelVersion("1.0.0"),
            types = listOf(typeString, typeMarkdown),
            entityDefs = listOf(personEntity, companyEntity),
            relationshipDefs = emptyList(), // TODO tests on model in memory relationships
            documentationHome = null,
            origin = ModelOrigin.Manual,
            hashtags = emptyList()
        )
    }
}