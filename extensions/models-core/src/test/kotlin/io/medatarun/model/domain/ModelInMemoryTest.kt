package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModelInMemoryTest {


    @Test
    fun `model contains person and company entities`() {
        val model = createModel()

        val markdownType = model.findType(TypeKey("Markdown"))

        val entityIds = model.entities.map { it.key }.toSet()
        assertEquals(setOf(EntityKey("person"), EntityKey("company")), entityIds)

        val person = model.entities.first { it.key == EntityKey("person") }
        assertEquals(5, person.countAttributes())

        val attrInfos = model.findEntityAttributeOptional(
            EntityRef.ByKey(EntityKey("person")),
            EntityAttributeRef.ByKey(AttributeKey("infos"))
        )
        assertNotNull(attrInfos)
        assertEquals(markdownType.id, attrInfos.typeId)

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

    private fun createModel(): ModelAggregateInMemory {

        val typeString = ModelTypeInMemory.of("String")
        val typeMarkdown = ModelTypeInMemory.of("Markdown")
        val personIdentifierAttributeId = AttributeId.generate()
        val personEntity = EntityInMemory.builder(
            key = EntityKey("person"),
            identifierAttributeId = personIdentifierAttributeId,
        ) {
            name = LocalizedTextNotLocalized("Person")
            addAttribute(
                AttributeInMemory(
                    id = personIdentifierAttributeId,
                    key = AttributeKey("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    typeId = typeString.id,
                    optional = false,
                    tags = emptyList()
                ),
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("firstName"),
                    name = LocalizedTextNotLocalized("First Name"),
                    description = null,
                    typeId = typeString.id,
                    optional = false,
                    tags = emptyList()
                ),
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("lastName"),
                    name = LocalizedTextNotLocalized("Last Name"),
                    description = null,
                    typeId = typeString.id,
                    optional = false,
                    tags = emptyList()
                ),
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("phoneNumber"),
                    name = LocalizedTextNotLocalized("Phone Number"),
                    description = null,
                    typeId = typeString.id,
                    optional = false,
                    tags = emptyList()
                ),
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("infos"),
                    name = LocalizedTextNotLocalized("Infos"),
                    description = null,
                    typeId = typeMarkdown.id,
                    optional = false,
                    tags = emptyList()
                )
            )
        }
        val companyIdentifierAttributeId = AttributeId.generate()
        val companyEntity = EntityInMemory.builder(
            key = EntityKey("company"),
            identifierAttributeId = companyIdentifierAttributeId,
        ) {
            name = LocalizedTextNotLocalized("Company")
            addAttribute(
                AttributeInMemory(
                    id = companyIdentifierAttributeId,
                    key = AttributeKey("id"),
                    name = LocalizedTextNotLocalized("Identifier"),
                    description = null,
                    typeId = typeString.id,
                    optional = false,
                    tags = emptyList()
                ),
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("name"),
                    name = LocalizedTextNotLocalized("Name"),
                    description = null,
                    typeId = typeString.id,
                    optional = false,
                    tags = emptyList()
                ),
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("location"),
                    name = LocalizedTextNotLocalized("Location"),
                    description = null,
                    typeId = typeString.id,
                    optional = true,
                    tags = emptyList()
                ),
                AttributeInMemory(
                    id = AttributeId.generate(),
                    key = AttributeKey("website"),
                    name = LocalizedTextNotLocalized("Website"),
                    description = null,
                    typeId = typeString.id,
                    optional = true,
                    tags = emptyList()
                )
            )

        }

        return ModelAggregateInMemory(
            model = ModelInMemory(
                id = ModelId.generate(),
                key = ModelKey("test-model"),
                name = LocalizedTextNotLocalized("Test Model"),
                description = null,
                version = ModelVersion("1.0.0"),
                documentationHome = null,
                origin = ModelOrigin.Manual,
            ),
            types = listOf(typeString, typeMarkdown),
            entities = listOf(personEntity, companyEntity),
            relationships = emptyList(), // TODO tests on model in memory relationships
            tags = emptyList()
        )
    }
}
