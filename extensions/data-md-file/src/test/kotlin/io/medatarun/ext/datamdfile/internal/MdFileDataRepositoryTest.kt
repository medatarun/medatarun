package io.medatarun.ext.datamdfile.internal

import io.medatarun.model.infra.ModelAttributeInMemory
import io.medatarun.model.infra.ModelEntityInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.LocalizedTextNotLocalized
import io.medatarun.model.model.ModelAttributeId
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelTypeId
import io.medatarun.model.model.ModelVersion
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class MdFileDataRepositoryTest {

    private val filesystemBuilder = FilesystemBuilder()
    private val repository = MdFileDataRepository(filesystemBuilder.storageDirectory())
    private val model = createModel()

    @Test
    fun `filesystem builder exposes expected directories`() {
        assertNotNull(repository)
        assertTrue(Files.isDirectory(filesystemBuilder.modelsDirectory()))
        assertTrue(Files.isDirectory(filesystemBuilder.storageDirectory()))
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
