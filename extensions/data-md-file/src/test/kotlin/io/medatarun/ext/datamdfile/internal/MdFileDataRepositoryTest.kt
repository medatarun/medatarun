package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.EntityInitializer
import io.medatarun.data.EntityInstanceId
import io.medatarun.data.EntityUpdater
import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.LocalizedTextNotLocalized
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelTypeId
import io.medatarun.model.model.ModelVersion
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.text.RegexOption

private val FRONTMATTER_REGEX = Regex(
    pattern = "^---\\s*\\n(.*?)\\n---\\s*\\n(.*)$",
    options = setOf(RegexOption.DOT_MATCHES_ALL)
)

private fun splitMarkdown(markdown: String): Pair<String, String> {
    val match = FRONTMATTER_REGEX.find(markdown)
        ?: error("Markdown frontmatter not found:\n$markdown")
    return match.groupValues[1] to match.groupValues[2]
}

private fun assertFrontmatterContains(frontmatter: String, key: String, expectedValue: String) {
    val pattern = Regex("^${Regex.escape(key)}:\\s*\"?${Regex.escape(expectedValue)}\"?$")
    val matches = frontmatter.lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .any { pattern.containsMatchIn(it) }
    assertTrue(matches, "Expected frontmatter to contain $key with value $expectedValue but was:\n$frontmatter")
}

internal class MdFileDataRepositoryTest {

    private val model = createModel()

    @Test
    fun `filesystem builder exposes expected directories`() {
        val filesystemFixture = FilesystemFixture()
        val repository = MdFileDataRepository(filesystemFixture.storageDirectory())

        assertNotNull(repository)
        assertTrue(Files.isDirectory(filesystemFixture.modelsDirectory()))
        assertTrue(Files.isDirectory(filesystemFixture.storageDirectory()))
    }

    @Test
    fun `create entity stores attributes in markdown frontmatter and markdown sections`() {
        val filesystemFixture = FilesystemFixture()
        val repository = MdFileDataRepository(filesystemFixture.storageDirectory())
        val entityId = "john-doe"
        val personEntityId = EntityDefId("person")

        repository.createEntity(
            model,
            personEntityId,
            MapEntityInitializer {
                attr["id"] = entityId
                attr["firstName"] = "John"
                attr["lastName"] = "Doe"
                attr["phoneNumber"] = "+33 1 23 45 67 89"
                attr["infos"] = "Works as developer.\n\n- Kotlin\n- Markdown"
            }
        )

        val entityPath = filesystemFixture.storageDirectory()
            .resolve(personEntityId.value)
            .resolve("$entityId.md")

        assertTrue(Files.exists(entityPath), "Expected Markdown file for created entity")

        val markdownContent = Files.readString(entityPath)
        val markdownParts = splitMarkdown(markdownContent)
        val frontmatter = markdownParts.first
        val body = markdownParts.second

        assertFrontmatterContains(frontmatter, "id", entityId)
        assertFrontmatterContains(frontmatter, "firstName", "John")
        assertFrontmatterContains(frontmatter, "lastName", "Doe")
        assertFrontmatterContains(frontmatter, "phoneNumber", "+33 1 23 45 67 89")
        assertFalse(
            frontmatter.lineSequence().any { it.trim().startsWith("infos:") },
            "Markdown attributes must not be persisted in frontmatter:\n$frontmatter"
        )

        assertTrue(body.contains("## infos"), "Markdown body must expose a section per markdown attribute")
        assertTrue(body.contains("Works as developer."), "Markdown body should keep attribute content")
        assertTrue(body.contains("- Kotlin"), "Markdown body should keep original markdown formatting")
    }

    @Test
    fun `update entity rewrites markdown with new values`() {
        val filesystemFixture = FilesystemFixture()
        val repository = MdFileDataRepository(filesystemFixture.storageDirectory())
        val entityId = "jane-doe"
        val personEntityId = EntityDefId("person")

        repository.createEntity(
            model,
            personEntityId,
            MapEntityInitializer {
                attr["id"] = entityId
                attr["firstName"] = "Jane"
                attr["lastName"] = "Doe"
                attr["phoneNumber"] = "+1 202 555 0102"
                attr["infos"] = "Original bio."
            }
        )

        repository.updateEntity(
            model,
            personEntityId,
            MapEntityUpdater(TestEntityInstanceId(entityId)) {
                update("lastName", "Doe-Smith")
                update("phoneNumber", "+1 202 555 0199")
                update("infos", "Updated bio.\n\nLoves tests.")
            }
        )

        val entityPath = filesystemFixture.storageDirectory()
            .resolve(personEntityId.value)
            .resolve("$entityId.md")

        val markdownContent = Files.readString(entityPath)
        val markdownParts = splitMarkdown(markdownContent)
        val frontmatter = markdownParts.first
        val body = markdownParts.second

        assertFrontmatterContains(frontmatter, "lastName", "Doe-Smith")
        assertFrontmatterContains(frontmatter, "phoneNumber", "+1 202 555 0199")
        assertTrue(body.contains("## infos"), "Markdown section must remain after update")
        assertTrue(body.contains("Updated bio."), "Markdown body should reflect updated value")
        assertFalse(body.contains("Original bio."), "Markdown body should not keep previous value")
    }

    @Test
    fun `delete entity removes markdown file`() {
        val filesystemFixture = FilesystemFixture()
        val repository = MdFileDataRepository(filesystemFixture.storageDirectory())
        val entityId = "acme-1"
        val companyEntityId = EntityDefId("company")

        repository.createEntity(
            model,
            companyEntityId,
            MapEntityInitializer {
                attr["id"] = entityId
                attr["name"] = "Acme"
                attr["location"] = "Paris"
                attr["website"] = "https://acme.test"
            }
        )

        val entityPath = filesystemFixture.storageDirectory()
            .resolve(companyEntityId.value)
            .resolve("$entityId.md")
        assertTrue(Files.exists(entityPath), "Expected Markdown file for created entity")

        repository.deleteEntity(
            model,
            companyEntityId,
            TestEntityInstanceId(entityId)
        )

        assertFalse(Files.exists(entityPath), "Markdown file should be removed after delete")
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

    private data class TestEntityInstanceId(
        private val value: String
    ) : EntityInstanceId {
        override fun asString(): String = value
    }

    private class MapEntityInitializer(
        build: Builder.() -> Unit
    ) : EntityInitializer {
        private val values: Map<AttributeDefId, Any?>

        init {
            val builder = Builder().apply(build)
            values = builder.toModelMap()
        }

        override fun <T> get(attributeId: AttributeDefId): T {
            if (!values.containsKey(attributeId)) {
                error("No data provided for attribute ${attributeId.value}")
            }
            @Suppress("UNCHECKED_CAST")
            return values[attributeId] as T
        }

        class Builder internal constructor() {
            val attr: MutableMap<String, Any?> = linkedMapOf()

            fun toModelMap(): Map<AttributeDefId, Any?> =
                attr.mapKeys { (key, _) -> AttributeDefId(key) }
        }
    }

    private class MapEntityUpdater(
        override val id: EntityInstanceId,
        build: Builder.() -> Unit
    ) : EntityUpdater {
        private val instructionsByAttribute: Map<AttributeDefId, EntityUpdater.Instruction>
        private val orderedInstructions: List<EntityUpdater.Instruction>

        init {
            val builder = Builder().apply(build)
            orderedInstructions = builder.instructions.toList()
            instructionsByAttribute = orderedInstructions.associateBy { it.attributeId }
        }

        override fun get(attributeId: AttributeDefId): EntityUpdater.Instruction =
            instructionsByAttribute[attributeId] ?: EntityUpdater.Instruction.InstructionNone(attributeId)

        override fun list(): List<EntityUpdater.Instruction> = orderedInstructions

        class Builder internal constructor() {
            internal val instructions: MutableList<EntityUpdater.Instruction> = mutableListOf()

            fun update(attribute: String, value: Any) {
                instructions += EntityUpdater.Instruction.InstructionUpdate(AttributeDefId(attribute), value)
            }

            fun none(attribute: String) {
                instructions += EntityUpdater.Instruction.InstructionNone(AttributeDefId(attribute))
            }
        }
    }
}
