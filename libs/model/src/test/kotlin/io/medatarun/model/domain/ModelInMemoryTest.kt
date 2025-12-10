package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelInMemoryTest {


    @Test
    fun `model contains person and company entities`() {
        val model = createModel()
        val entityIds = model.entityDefs.map { it.id }.toSet()
        assertEquals(setOf(EntityDefId("person"), EntityDefId("company")), entityIds)

        val person = model.entityDefs.first { it.id == EntityDefId("person") }
        assertEquals(5, person.countAttributeDefs())
        assertEquals(ModelTypeId("Markdown"), person.getAttributeDef(AttributeDefId("infos")).type)

        val company = model.entityDefs.first { it.id == EntityDefId("company") }
        assertTrue(company.getAttributeDef(AttributeDefId("location")).optional)
        assertTrue(company.getAttributeDef(AttributeDefId("website")).optional)
    }

    private fun createModel(): ModelInMemory {

        val typeString = ModelTypeInMemory.of("String")
        val typeMarkdown = ModelTypeInMemory.of("Markdown")

        val personEntity = EntityDefInMemory.builder(
            id = EntityDefId("person"),
            identifierAttributeDefId = AttributeDefId("id"),
        ) {
            name = LocalizedTextNotLocalized("Person")
            addAttribute(
                AttributeDefInMemory(
                    id = AttributeDefId("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = typeString.id,
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("firstName"),
                    name = LocalizedTextNotLocalized("First Name"),
                    description = null,
                    type = typeString.id,
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("lastName"),
                    name = LocalizedTextNotLocalized("Last Name"),
                    description = null,
                    type = typeString.id,
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("phoneNumber"),
                    name = LocalizedTextNotLocalized("Phone Number"),
                    description = null,
                    type = typeString.id,
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("infos"),
                    name = LocalizedTextNotLocalized("Infos"),
                    description = null,
                    type = typeMarkdown.id,
                    optional = false
                )
            )
        }

        val companyEntity = EntityDefInMemory.builder(
            id = EntityDefId("company"),
            identifierAttributeDefId = AttributeDefId("id"),
        ) {
            name = LocalizedTextNotLocalized("Company")
            addAttribute(
                AttributeDefInMemory(
                    id = AttributeDefId("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = typeString.id,
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("name"),
                    name = LocalizedTextNotLocalized("Name"),
                    description = null,
                    type = typeString.id,
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("location"),
                    name = LocalizedTextNotLocalized("Location"),
                    description = null,
                    type = typeString.id,
                    optional = true
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("website"),
                    name = LocalizedTextNotLocalized("Website"),
                    description = null,
                    type = typeString.id,
                    optional = true
                )
            )

        }

        return ModelInMemory(
            id = ModelId("test-model"),
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