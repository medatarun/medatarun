package io.medatarun.model.model

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
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
        assertEquals(5, person.countAttributes())
        assertEquals(ModelTypeId("Markdown"), person.getAttribute(AttributeDefId("infos")).type)

        val company = model.entityDefs.first { it.id == EntityDefId("company") }
        assertTrue(company.getAttribute(AttributeDefId("location")).optional)
        assertTrue(company.getAttribute(AttributeDefId("website")).optional)
    }

    private fun createModel(): ModelInMemory {
        val personEntity = EntityDefInMemory(
            id = EntityDefId("person"),
            name = LocalizedTextNotLocalized("Person"),
            description = null,
            attributes = listOf(
                AttributeDefInMemory(
                    id = AttributeDefId("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("firstName"),
                    name = LocalizedTextNotLocalized("First Name"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("lastName"),
                    name = LocalizedTextNotLocalized("Last Name"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("phoneNumber"),
                    name = LocalizedTextNotLocalized("Phone Number"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("infos"),
                    name = LocalizedTextNotLocalized("Infos"),
                    description = null,
                    type = ModelTypeId("Markdown"),
                    optional = false
                )
            )
        )

        val companyEntity = EntityDefInMemory(
            id = EntityDefId("company"),
            name = LocalizedTextNotLocalized("Company"),
            description = null,
            attributes = listOf(
                AttributeDefInMemory(
                    id = AttributeDefId("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("name"),
                    name = LocalizedTextNotLocalized("Name"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = false
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("location"),
                    name = LocalizedTextNotLocalized("Location"),
                    description = null,
                    type = ModelTypeId("String"),
                    optional = true
                ),
                AttributeDefInMemory(
                    id = AttributeDefId("website"),
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
            entityDefs = listOf(personEntity, companyEntity)
        )
    }
}