package io.medatarun.model.domain

import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModelAggregateInMemoryTest {


    @Test
    fun `model contains person and company entities`() {
        val model = createModel()

        val markdownType = model.findType(TypeKey("Markdown"))

        val entityIds = model.entities.map { it.key }.toSet()
        assertEquals(setOf(EntityKey("person"), EntityKey("company")), entityIds)

        val person = model.entities.first { it.key == EntityKey("person") }
        assertEquals(5, model.countAttributes(person.id))

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
        val personEntityId = EntityId.generate()
        val personEntity = EntityInMemory.builder(
            key = EntityKey("person"),
        ) {
            id = personEntityId
            name = TextSingleLine("Person")
        }
        val personAttributes = listOf(
            AttributeInMemory(
                id = personIdentifierAttributeId,
                key = AttributeKey("id"),
                name = TextSingleLine("Identifier"),
                description = null,
                typeId = typeString.id,
                optional = false,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(personEntity.id)
            ),
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey("firstName"),
                name = TextSingleLine("First Name"),
                description = null,
                typeId = typeString.id,
                optional = false,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(personEntity.id)
            ),
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey("lastName"),
                name = TextSingleLine("Last Name"),
                description = null,
                typeId = typeString.id,
                optional = false,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(personEntity.id)
            ),
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey("phoneNumber"),
                name = TextSingleLine("Phone Number"),
                description = null,
                typeId = typeString.id,
                optional = false,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(personEntity.id)
            ),
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey("infos"),
                name = TextSingleLine("Infos"),
                description = null,
                typeId = typeMarkdown.id,
                optional = false,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(personEntity.id)
            )
        )

        val companyIdentifierAttributeId = AttributeId.generate()
        val companyEntityId = EntityId.generate()
        val companyEntity = EntityInMemory.builder(
            key = EntityKey("company"),
        ) {
            id = companyEntityId
            name = TextSingleLine("Company")


        }

        val companyAttributes = listOf(
            AttributeInMemory(
                id = companyIdentifierAttributeId,
                key = AttributeKey("id"),
                name = TextSingleLine("Identifier"),
                description = null,
                typeId = typeString.id,
                optional = false,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(companyEntity.id)
            ),
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey("name"),
                name = TextSingleLine("Name"),
                description = null,
                typeId = typeString.id,
                optional = false,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(companyEntity.id)
            ),
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey("location"),
                name = TextSingleLine("Location"),
                description = null,
                typeId = typeString.id,
                optional = true,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(companyEntity.id)
            ),
            AttributeInMemory(
                id = AttributeId.generate(),
                key = AttributeKey("website"),
                name = TextSingleLine("Website"),
                description = null,
                typeId = typeString.id,
                optional = true,
                tags = emptyList(),
                ownerId = AttributeOwnerId.OwnerEntityId(companyEntity.id)
            )
        )

        return ModelAggregateInMemory(
            model = ModelInMemory(
                id = ModelId.generate(),
                key = ModelKey("test-model"),
                name = TextSingleLine("Test Model"),
                description = null,
                version = ModelVersion("1.0.0"),
                documentationHome = null,
                origin = ModelOrigin.Manual,
                authority = ModelAuthority.SYSTEM,
            ),
            types = listOf(typeString, typeMarkdown),
            entities = listOf(personEntity, companyEntity),
            attributes = companyAttributes + personAttributes,
            relationships = emptyList(), // TODO tests on model in memory relationships
            tags = emptyList(),
            entityPrimaryKeys = listOf(
                EntityPrimaryKeyInMemory.ofSingleAttribute(personEntityId, personIdentifierAttributeId),
                EntityPrimaryKeyInMemory.ofSingleAttribute(companyEntityId, companyIdentifierAttributeId),
            ),
            businessKeys = emptyList()
        )
    }
}
