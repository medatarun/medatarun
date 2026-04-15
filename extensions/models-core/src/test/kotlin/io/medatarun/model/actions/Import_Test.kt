package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelImporterData
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.ResourceLocator
import io.medatarun.tags.core.domain.Tag
import io.medatarun.type.commons.key.Key
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class Import_Test {
    @Test
    fun `import then imported`() {

        // we do not need to test everything because import is like copy
        // and copy is also covered. We just check the basics

        val modelRef = ModelRef.modelRefKey("recipe")
        val sampleModel = ModelAggregateInMemory(
            ModelInMemory(
                id = ModelId.generate(),
                key = modelRef.key,
                name = null,
                description = null,
                version = ModelVersion("1.2.3"),
                origin = ModelOrigin.Uri(URI("test:recipe")),
                authority = ModelAuthority.SYSTEM,
                documentationHome = null
            ),
            types = emptyList(),
            entities = emptyList(),
            attributes = emptyList(),
            relationships = emptyList(),
            tags = emptyList(),
            entityPrimaryKeys = emptyList(),
            businessKeys = emptyList()
        )
        val env = ModelTestEnv(
            listOf(ImportFixtureExtension(sampleModel, emptyList()))
        )
        env.dispatch(ModelAction.Import("test:recipe", null, null))
        val found = env.queries.findModelRoot(modelRef)
        assertEquals(sampleModel.id, found.id)
        assertEquals(sampleModel.key, found.key)
        assertEquals(sampleModel.name, found.name)
        assertEquals(sampleModel.description, found.description)
        assertEquals(sampleModel.version, found.version)
        assertEquals(sampleModel.origin, found.origin)
        assertEquals(sampleModel.authority, found.authority)
        assertEquals(sampleModel.documentationHome, found.documentationHome)

        // Importing a model immediately creates a first version
        env.assertUniqueVersion(sampleModel.version, found.id)

    }

    private class ImportFixtureExtension(private val sampleModel: ModelAggregate, private val sampleTags: List<Tag>) :
        MedatarunExtension {
        override val id: ExtensionId = "import-fixture"
        override fun initContributions(ctx: MedatarunExtensionCtx) {
            ctx.registerContribution(ModelImporter::class, ImporterFixture(sampleModel, sampleTags))
        }
    }

    private class ImporterFixture(private val sampleModel: ModelAggregate, private val sampleTags: List<Tag>) :
        ModelImporter {
        override fun accept(path: String, resourceLocator: ResourceLocator): Boolean = path.startsWith("test:")

        override fun toModel(
            path: String,
            resourceLocator: ResourceLocator,
            modelKeyChoosen: ModelKey?,
            modelNameChoosen: String?
        ): ModelImporterData {
            if (path == "test:recipe") {
                return ModelImporterData(
                    model = sampleModel,
                    tags = sampleTags
                )
            } else throw IllegalStateException("Illega path $path")
        }
    }

}