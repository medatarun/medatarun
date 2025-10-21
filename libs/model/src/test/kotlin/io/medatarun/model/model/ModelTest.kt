package io.medatarun.model.model

import io.medatarun.model.infra.ModelRepositoryInMemory
import io.medatarun.model.infra.ModelStorageAmbiguousException
import io.medatarun.model.infra.ModelStorageCompositeNoRepositoryException
import io.medatarun.model.infra.ModelStoragesComposite
import io.medatarun.model.internal.ModelCmdImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.ports.RepositoryRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ModelTest {
    val repo1 = ModelRepositoryInMemory("repo1")
    val repo2 = ModelRepositoryInMemory("repo2")
    val storages = ModelStoragesComposite(listOf(repo1, repo2))
    val query: ModelQueries = ModelQueriesImpl(storages)
    val cmd: ModelCmd = ModelCmdImpl(storages)



    @Test
    fun `can not instantiate storages without repositories`() {
        assertFailsWith(ModelStorageCompositeNoRepositoryException::class) {
            ModelStoragesComposite(emptyList())
        }
    }

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
}

