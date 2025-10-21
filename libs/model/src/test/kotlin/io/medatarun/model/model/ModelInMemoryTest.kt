package io.medatarun.model.model

import io.medatarun.model.infra.ModelAttributeInMemory
import io.medatarun.model.infra.ModelEntityInMemory
import io.medatarun.model.infra.ModelInMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelInMemoryTest {

    @Test
    fun `model contains person and company entities`() {
        val model = createModel()
        val entityIds = model.entities.map { it.id }.toSet()
        assertEquals(setOf(ModelEntityId("person"), ModelEntityId("company")), entityIds)

        val person = model.entities.first { it.id == ModelEntityId("person") }
        assertEquals(5, person.countAttributes())
        assertEquals(ModelTypeId("Markdown"), person.getAttribute(ModelAttributeId("infos")).type)

        val company = model.entities.first { it.id == ModelEntityId("company") }
        assertTrue(company.getAttribute(ModelAttributeId("location")).optional)
        assertTrue(company.getAttribute(ModelAttributeId("website")).optional)
    }

    private fun createModel(): ModelInMemory {
        val personEntity = ModelEntityInMemory(
            id = ModelEntityId("person"),
            name = LocalizedTextNotLocalized("Person"),
            description = null,
            attributes = listOf(
                ModelAttributeInMemory(
                    id = ModelAttributeId("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                ModelAttributeInMemory(
                    id = ModelAttributeId("firstName"),
                    name = LocalizedTextNotLocalized("First Name"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                ModelAttributeInMemory(
                    id = ModelAttributeId("lastName"),
                    name = LocalizedTextNotLocalized("Last Name"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                ModelAttributeInMemory(
                    id = ModelAttributeId("phoneNumber"),
                    name = LocalizedTextNotLocalized("Phone Number"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                ModelAttributeInMemory(
                    id = ModelAttributeId("infos"),
                    name = LocalizedTextNotLocalized("Infos"),
                    description = null,
                    type = ModelTypeId("Markdown"),
                    optional = false
                )
            )
        )

        val companyEntity = ModelEntityInMemory(
            id = ModelEntityId("company"),
            name = LocalizedTextNotLocalized("Company"),
            description = null,
            attributes = listOf(
                ModelAttributeInMemory(
                    id = ModelAttributeId("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                ModelAttributeInMemory(
                    id = ModelAttributeId("name"),
                    name = LocalizedTextNotLocalized("Name"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                ModelAttributeInMemory(
                    id = ModelAttributeId("location"),
                    name = LocalizedTextNotLocalized("Location"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = true
                ),
                ModelAttributeInMemory(
                    id = ModelAttributeId("website"),
                    name = LocalizedTextNotLocalized("Website"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = true
                )
            )
        )

        return ModelInMemory(
            id = ModelId("test-model"),
            name = LocalizedTextNotLocalized("Test Model"),
            description = null,
            version = ModelVersion("1.0.0"),
            entities = listOf(personEntity, companyEntity)
        )
    }
}