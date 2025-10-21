package io.medatarun.model.model

import io.medatarun.model.infra.*
import io.medatarun.model.internal.ModelCmdImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.internal.UpdateEntityDefIdDuplicateIdException
import io.medatarun.model.ports.RepositoryRef
import io.medatarun.model.ports.RepositoryRef.Companion.ref
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ModelTest {

    @Test
    fun `can not instantiate storages without repositories`() {
        assertFailsWith(ModelStorageCompositeNoRepositoryException::class) {
            ModelStoragesComposite(emptyList())
        }
    }

    // ------------------------------------------------------------------------
    // Create models
    // ------------------------------------------------------------------------

    @Test
    fun `create model fail with ambiguous storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val storages = ModelStoragesComposite(listOf(repo1, repo2))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        assertFailsWith(ModelStorageAmbiguousException::class) {
            cmd.createModel(
                ModelId("m1"),
                LocalizedTextNotLocalized("M1"),
                null,
                ModelVersion("1.0.0")
            )
        }
    }

    @Test
    fun `create model ok with one storage mode auto`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val storages = ModelStoragesComposite(listOf(repo1))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val modelId = ModelId("m1")
        assertDoesNotThrow {
            cmd.createModel(
                modelId,
                LocalizedTextNotLocalized("M1"),
                null,
                ModelVersion("1.0.0")
            )
        }
        assertNotNull(repo1.findModelByIdOptional(modelId))
    }

    @Test
    fun `create model ok with multiple storages and specified storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val storages = ModelStoragesComposite(listOf(repo1, repo2))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)

        val modelId = ModelId("m1")
        cmd.createModel(
            modelId,
            LocalizedTextNotLocalized("M1"),
            null,
            ModelVersion("1.0.0"),
            RepositoryRef.Id(repo2.repositoryId)
        )
        assertDoesNotThrow { query.findModelById(modelId) }

        assertNull(repo1.findModelByIdOptional(modelId))
        assertNotNull(repo2.findModelByIdOptional(modelId))

    }

    @Test
    fun `create model with name description and version when present`() {
        val repo = ModelRepositoryInMemory("repo")
        val storages = ModelStoragesComposite(listOf(repo))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)

        val modelId = ModelId("m-")
        val name = LocalizedTextNotLocalized("Model name")
        val description = LocalizedMarkdownNotLocalized("Model description")
        val version = ModelVersion("2.0.0")

        cmd.createModel(modelId, name, description, version)

        val reloaded = query.findModelById(modelId)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(version, reloaded.version)
    }

    @Test
    fun `create model keeps values without optional description`() {
        val repo = ModelRepositoryInMemory("repo")
        val storages = ModelStoragesComposite(listOf(repo))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)

        val modelId = ModelId("m")
        val name = LocalizedTextNotLocalized("Model without description")
        val version = ModelVersion("3.0.0")

        cmd.createModel(modelId, name, null, version)

        val reloaded = query.findModelById(modelId)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
        assertEquals(version, reloaded.version)
    }

    // ------------------------------------------------------------------------
    // Update models
    // ------------------------------------------------------------------------

    @Test
    fun `updates on model fails if model not found`() {
        val repo = ModelRepositoryInMemory("repo1")
        val storages = ModelStoragesComposite(listOf(repo))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)

        val modelId = ModelId("m1")
        cmd.createModel(modelId, LocalizedTextNotLocalized("Model name"), null, ModelVersion("2.0.0"))
        val modelIdWrong = ModelId("m2")
        assertFailsWith(ModelNotFoundException::class) {
            cmd.updateModelName(
                modelIdWrong,
                LocalizedTextNotLocalized("other")
            )
        }
        assertFailsWith(ModelNotFoundException::class) {
            cmd.updateModelDescription(
                modelIdWrong,
                LocalizedTextNotLocalized("other description")
            )
        }
        assertFailsWith(ModelNotFoundException::class) { cmd.updateModelVersion(modelIdWrong, ModelVersion("3.0.0")) }
        cmd.updateModelName(modelId, LocalizedTextNotLocalized("Model name 2"))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), query.findModelById(modelId).name)
    }

    class TestEnvOneModel() {
        val repo = ModelRepositoryInMemory("repo1")
        val storages = ModelStoragesComposite(listOf(repo))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)
        val modelId = ModelId("m1")
        init {
            cmd.createModel(modelId, LocalizedTextNotLocalized("Model name"), null, ModelVersion("2.0.0"))
        }
    }

    class TestEnvEntityUpdate {
        val repo = ModelRepositoryInMemory("repo-entity-update")
        val storages = ModelStoragesComposite(listOf(repo))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)
        val modelId = ModelId("model-entity-update")
        val primaryEntityId = EntityDefId("entity-primary")
        val secondaryEntityId = EntityDefId("entity-secondary")

        init {
            cmd.createModel(
                modelId,
                LocalizedTextNotLocalized("Model entity update"),
                null,
                ModelVersion("1.0.0")
            )
            cmd.createEntityDef(
                modelId,
                primaryEntityId,
                LocalizedTextNotLocalized("Entity primary"),
                LocalizedMarkdownNotLocalized("Entity primary description")
            )
            cmd.createEntityDef(
                modelId,
                secondaryEntityId,
                LocalizedTextNotLocalized("Entity secondary"),
                LocalizedMarkdownNotLocalized("Entity secondary description")
            )
        }
    }

    @Test
    fun `updates on model name persists the name`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelName(env.modelId, LocalizedTextNotLocalized("Model name 2"))
        assertEquals(LocalizedTextNotLocalized("Model name 2"), env.query.findModelById(env.modelId).name)
    }

    @Test
    fun `updates on model description persists the description`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelDescription(env.modelId, LocalizedTextNotLocalized("Model description 2"))
        assertEquals(LocalizedTextNotLocalized("Model description 2"), env.query.findModelById(env.modelId).description)
    }

    @Test
    fun `updates on model description to null persists the description`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelDescription(env.modelId, LocalizedTextNotLocalized("Model description 2"))
        env.cmd.updateModelDescription(env.modelId, null)
        assertNull(env.query.findModelById(env.modelId).description)
    }

    @Test
    fun `updates on model version persists the version`() {
        val env = TestEnvOneModel()
        env.cmd.updateModelVersion(env.modelId, ModelVersion("4.5.6"))
        assertEquals(ModelVersion("4.5.6"), env.query.findModelById(env.modelId).version)
    }

    // ------------------------------------------------------------------------
    // Delete models
    // ------------------------------------------------------------------------

    @Test
    fun `delete model fails if model Id not found in any storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val storages = ModelStoragesComposite(listOf(repo1, repo2))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        cmd.createModel(
            id = ModelId("m-to-delete-repo-1"),
            name = LocalizedTextNotLocalized("Model to delete"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo2.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-delete-repo-2"),
            name = LocalizedTextNotLocalized("Model to delete 2 on repo 2"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo2.repositoryId.ref()
        )
        assertThrows<ModelNotFoundException> {
            cmd.deleteModel(ModelId("m-to-delete-repo-3"))
        }
    }

    @Test
    fun `delete model removes it from storage`() {
        val repo1 = ModelRepositoryInMemory("repo1")
        val repo2 = ModelRepositoryInMemory("repo2")
        val storages = ModelStoragesComposite(listOf(repo1, repo2))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)

        cmd.createModel(
            id = ModelId("m-to-delete-repo-1"),
            name = LocalizedTextNotLocalized("Model to delete"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo1.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-preserve-repo-1"),
            name = LocalizedTextNotLocalized("Model to preserve"),
            description = null,
            version = ModelVersion("0.1.0"),
            repositoryRef = repo1.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-delete-repo-2"),
            name = LocalizedTextNotLocalized("Model to delete 2 on repo 2"),
            description = null,
            version = ModelVersion("0.0.1"),
            repositoryRef = repo2.repositoryId.ref()
        )
        cmd.createModel(
            id = ModelId("m-to-preserve-repo-2"),
            name = LocalizedTextNotLocalized("Model to preserve on repo 2"),
            description = null,
            version = ModelVersion("0.1.0"),
            repositoryRef = repo2.repositoryId.ref()
        )

        cmd.deleteModel(ModelId("m-to-delete-repo-1"))
        assertNull(repo1.findModelByIdOptional(ModelId("m-to-delete-repo-1")))
        assertFailsWith<ModelNotFoundException> {
            query.findModelById(ModelId("m-to-delete-repo-1"))
        }
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-1")))
        assertNotNull(query.findModelById(ModelId("m-to-delete-repo-2")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-2")))

        cmd.deleteModel(ModelId("m-to-delete-repo-2"))
        assertNull(repo1.findModelByIdOptional(ModelId("m-to-delete-repo-2")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-1")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-2")))

        cmd.deleteModel(ModelId("m-to-preserve-repo-1"))
        assertNull(repo2.findModelByIdOptional(ModelId("m-to-delete-repo-2")))
        assertNotNull(query.findModelById(ModelId("m-to-preserve-repo-2")))

        cmd.deleteModel(ModelId("m-to-preserve-repo-2"))
        assertNull(repo2.findModelByIdOptional(ModelId("m-to-delete-repo-2")))

    }

    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    @Test
    fun `create entity then id name and description shall persist`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity")
        val name = LocalizedTextNotLocalized("Order")
        val description = LocalizedMarkdownNotLocalized("Order description")

        env.cmd.createEntityDef(env.modelId, entityId, name, description)

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(entityId)
        assertEquals(entityId, reloaded.id)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
    }

    @Test
    fun `create entity with null name then name shall be null`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity-null-name")
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.cmd.createEntityDef(env.modelId, entityId, null, description)

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(entityId)
        assertEquals(entityId, reloaded.id)
        assertNull(reloaded.name)
        assertEquals(description, reloaded.description)
    }

    @Test
    fun `create entity with null description then description shall be null`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity-null-description")
        val name = LocalizedTextNotLocalized("Entity without description")

        env.cmd.createEntityDef(env.modelId, entityId, name, null)

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(entityId)
        assertEquals(entityId, reloaded.id)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
    }


    @Test
    fun `update entity with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongModelId = ModelId("unknown-model")

        assertFailsWith<ModelNotFoundException> {
            env.cmd.updateEntityDef(
                wrongModelId,
                env.primaryEntityId,
                EntityDefUpdateCmd.Name(LocalizedTextNotLocalized("Updated name"))
            )
        }
    }

    @Test
    fun `update entity with wrong entity id throws EntityDefNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongEntityId = EntityDefId("unknown-entity")

        assertFailsWith<EntityDefNotInModelException> {
            env.cmd.updateEntityDef(
                env.modelId,
                wrongEntityId,
                EntityDefUpdateCmd.Name(LocalizedTextNotLocalized("Updated name"))
            )
        }
    }

    @Test
    fun `update entity id with duplicate id throws exception`() {
        val env = TestEnvEntityUpdate()
        val duplicateId = env.secondaryEntityId

        assertFailsWith<UpdateEntityDefIdDuplicateIdException> {
            env.cmd.updateEntityDef(
                env.modelId,
                env.primaryEntityId,
                EntityDefUpdateCmd.Id(duplicateId)
            )
        }
    }

    @Test
    fun `update entity id with correct id ok`() {
        val env = TestEnvEntityUpdate()
        val newId = EntityDefId("entity-renamed")

        env.cmd.updateEntityDef(
            env.modelId,
            env.primaryEntityId,
            EntityDefUpdateCmd.Id(newId)
        )

        val reloaded = env.query.findModelById(env.modelId)
        assertNull(reloaded.findEntityDefOptional(env.primaryEntityId))
        assertNotNull(reloaded.findEntityDefOptional(newId))
    }

    @Test
    fun `update entity name not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newName = LocalizedTextNotLocalized("Entity primary updated")

        env.cmd.updateEntityDef(
            env.modelId,
            env.primaryEntityId,
            EntityDefUpdateCmd.Name(newName)
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update entity name null then name is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.updateEntityDef(
            env.modelId,
            env.primaryEntityId,
            EntityDefUpdateCmd.Name(null)
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertNull(reloaded.name)
    }

    @Test
    fun `update entity description not null persisted`() {
        val env = TestEnvEntityUpdate()
        val newDescription = LocalizedMarkdownNotLocalized("Primary entity updated description")

        env.cmd.updateEntityDef(
            env.modelId,
            env.primaryEntityId,
            EntityDefUpdateCmd.Description(newDescription)
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertEquals(newDescription, reloaded.description)
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = TestEnvEntityUpdate()

        env.cmd.updateEntityDef(
            env.modelId,
            env.primaryEntityId,
            EntityDefUpdateCmd.Description(null)
        )

        val reloaded = env.query.findModelById(env.modelId).findEntityDef(env.primaryEntityId)
        assertNull(reloaded.description)
    }


    @Test
    fun `delete entity in model then entity removed`() {
        val env = TestEnvOneModel()
        val entityId = EntityDefId("entity-to-delete")

        env.cmd.createEntityDef(env.modelId, entityId, LocalizedTextNotLocalized("To delete"), null)
        env.cmd.deleteEntityDef(env.modelId, entityId)

        val reloaded = env.query.findModelById(env.modelId)
        assertNull(reloaded.findEntityDefOptional(entityId))
    }

    @Test
    fun `delete entity with same id in two models then only entity in the specified model is removed`() {
        val repo = ModelRepositoryInMemory("repo")
        val storages = ModelStoragesComposite(listOf(repo))
        val cmd: ModelCmd = ModelCmdImpl(storages)
        val query: ModelQueries = ModelQueriesImpl(storages)

        val modelId1 = ModelId("model-1")
        val modelId2 = ModelId("model-2")
        val entityId = EntityDefId("shared-entity")

        cmd.createModel(
            modelId1,
            LocalizedTextNotLocalized("Model 1"),
            null,
            ModelVersion("1.0.0")
        )
        cmd.createModel(
            modelId2,
            LocalizedTextNotLocalized("Model 2"),
            null,
            ModelVersion("1.0.0")
        )
        cmd.createEntityDef(modelId1, entityId, LocalizedTextNotLocalized("Entity"), null)
        cmd.createEntityDef(modelId2, entityId, LocalizedTextNotLocalized("Entity"), null)

        cmd.deleteEntityDef(modelId1, entityId)

        val reloadedModel1 = query.findModelById(modelId1)
        val reloadedModel2 = query.findModelById(modelId2)

        assertNull(reloadedModel1.findEntityDefOptional(entityId))
        assertNotNull(reloadedModel2.findEntityDefOptional(entityId))
    }

}
