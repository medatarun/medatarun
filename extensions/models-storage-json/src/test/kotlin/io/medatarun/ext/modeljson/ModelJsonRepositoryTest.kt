package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.internal.ModelJsonConverter
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonFiles
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonRepository
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelRepositoryId
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.io.path.exists
import kotlin.test.*

class ModelJsonRepositoryTest {
    private val converter = ModelJsonConverter(true)

    internal inner class TestEnv {
        val fs = ModelJsonFilesystemFixture()
        val repositoryPath = fs.modelsDirectory()
        val files = ModelsStorageJsonFiles(repositoryPath)
        val repo: ModelsStorageJsonRepository = ModelsStorageJsonRepository(files, converter)
        val sampleModel = converter.fromJson(sampleModelJson)
        val sampleModel2 = sampleModel.copy(key = ModelKey(sampleModel.key.value + "-2"))
        fun importSample() {
            repo.persistModel(sampleModel)
        }

        fun importSample2() {
            repo.persistModel(sampleModel2)
        }
    }

    // ------------------------------------------------------------------------
    // Matching
    // ------------------------------------------------------------------------

    @Test
    fun testMatches() {
        val env = TestEnv()
        assertFalse(env.repo.matchesId(ModelRepositoryId("abc")))
        assertTrue(env.repo.matchesId(ModelsStorageJsonRepository.REPOSITORY_ID))
    }

    // ------------------------------------------------------------------------
    // Find
    // ------------------------------------------------------------------------

    @Test
    fun `findModelById not found`() {
        val env = TestEnv()
        assertNull(env.repo.findModelByKeyOptional(ModelKey("unknwon")))
        env.importSample()
        assertNull(env.repo.findModelByKeyOptional(ModelKey("unknwon")))
        env.importSample()
    }

    @Test
    fun `findModelById found`() {
        val env = TestEnv()
        assertNull(env.repo.findModelByKeyOptional(env.sampleModel.key))
        env.importSample()
        env.importSample2()
        val m = assertNotNull(env.repo.findModelByKeyOptional(env.sampleModel.key))
        assertEquals(env.sampleModel.key, m.key)
        val m2 = assertNotNull(env.repo.findModelByKeyOptional(env.sampleModel2.key))
        assertEquals(env.sampleModel2.key, m2.key)
    }

    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    @Test
    fun `create then path is correct`() {
        val env = TestEnv()
        env.repo.dispatch(ModelRepoCmd.CreateModel(env.sampleModel))
        env.repo.dispatch(ModelRepoCmd.CreateModel(env.sampleModel2))
        val path1 = env.fs.modelsDirectory().resolve(env.sampleModel.key.value + ".json")
        assertTrue(path1.exists())
        val path2 = env.fs.modelsDirectory().resolve(env.sampleModel2.key.value + ".json")
        assertTrue(path2.exists())
    }

    @Test
    fun `findAllModelIds empty`() {
        val env = TestEnv()
        val ids = env.repo.findAllModelIds()
        assertTrue(ids.isEmpty())

    }

    @Test
    fun `findAllModelIds one model`() {
        val env = TestEnv()
        env.importSample()
        val ids = env.repo.findAllModelIds()
        assertEquals(1, ids.size)
        assertTrue(ids.contains(env.sampleModel.id))
    }

    @Test
    fun `findAllModelIds two models`() {
        val env = TestEnv()
        env.importSample()
        env.importSample2()
        val ids = env.repo.findAllModelIds()
        assertEquals(2, ids.size)
        assertTrue(ids.contains(env.sampleModel.id))
        assertTrue(ids.contains(env.sampleModel2.id))
    }



    @Test
    fun `delete then no file left`() {
        val env = TestEnv()
        env.repo.dispatch(ModelRepoCmd.CreateModel(env.sampleModel))
        env.repo.dispatch(ModelRepoCmd.CreateModel(env.sampleModel2))
        env.repo.dispatch(ModelRepoCmd.DeleteModel(env.sampleModel.id))
        val path1 = env.fs.modelsDirectory().resolve(env.sampleModel.key.value + ".json")
        val path2 = env.fs.modelsDirectory().resolve(env.sampleModel2.key.value + ".json")
        assertFalse(path1.exists())
        assertTrue(path2.exists())
        env.repo.dispatch(ModelRepoCmd.DeleteModel(env.sampleModel2.id))
        assertFalse(path2.exists())


    }
}

