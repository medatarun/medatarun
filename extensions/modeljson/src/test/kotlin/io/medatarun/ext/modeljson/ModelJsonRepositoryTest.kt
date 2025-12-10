package io.medatarun.ext.modeljson

import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelRepositoryId
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.io.path.exists
import kotlin.test.*

class ModelJsonRepositoryTest {
    private val converter = ModelJsonConverter(true)

    internal inner class TestEnv {
        val fs = ModelJsonFilesystemFixture()
        val repositoryPath = fs.modelsDirectory()
        val repo: ModelJsonRepository = ModelJsonRepository(repositoryPath, converter)
        val sampleModel = converter.fromJson(sampleModelJson)
        val sampleModel2 = sampleModel.copy(id = ModelId(sampleModel.id.value + "-2"))
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
        assertTrue(env.repo.matchesId(ModelJsonRepository.REPOSITORY_ID))
    }

    // ------------------------------------------------------------------------
    // Find
    // ------------------------------------------------------------------------

    @Test
    fun `findModelById not found`() {
        val env = TestEnv()
        assertNull(env.repo.findModelByIdOptional(ModelId("unknwon")))
        env.importSample()
        assertNull(env.repo.findModelByIdOptional(ModelId("unknwon")))
        env.importSample()
    }

    @Test
    fun `findModelById found`() {
        val env = TestEnv()
        assertNull(env.repo.findModelByIdOptional(env.sampleModel.id))
        env.importSample()
        env.importSample2()
        val m = assertNotNull(env.repo.findModelByIdOptional(env.sampleModel.id))
        assertEquals(env.sampleModel.id, m.id)
        val m2 = assertNotNull(env.repo.findModelByIdOptional(env.sampleModel2.id))
        assertEquals(env.sampleModel2.id, m2.id)
    }

    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    @Test
    fun `create then path is correct`() {
        val env = TestEnv()
        env.repo.dispatch(ModelRepositoryCmd.CreateModel(env.sampleModel))
        env.repo.dispatch(ModelRepositoryCmd.CreateModel(env.sampleModel2))
        val path1 = env.fs.modelsDirectory().resolve(env.sampleModel.id.value + ".json")
        assertTrue(path1.exists())
        val path2 = env.fs.modelsDirectory().resolve(env.sampleModel2.id.value + ".json")
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
    fun `update name`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update description`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update version`() {
        TODO("Not yet implemented")
    }
    @Test
    fun `update model documentation home`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update model origin`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `delete then no file left`() {
        val env = TestEnv()
        env.repo.dispatch(ModelRepositoryCmd.CreateModel(env.sampleModel))
        env.repo.dispatch(ModelRepositoryCmd.CreateModel(env.sampleModel2))
        env.repo.dispatch(ModelRepositoryCmd.DeleteModel(env.sampleModel.id))
        val path1 = env.fs.modelsDirectory().resolve(env.sampleModel.id.value + ".json")
        val path2 = env.fs.modelsDirectory().resolve(env.sampleModel2.id.value + ".json")
        assertFalse(path1.exists())
        assertTrue(path2.exists())
        env.repo.dispatch(ModelRepositoryCmd.DeleteModel(env.sampleModel2.id))
        assertFalse(path2.exists())


    }
    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------


    @Test
    fun `create type`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update type name`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update type description`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `delete type`() {
        TODO("Not yet implemented")
    }
    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------


    @Test
    fun `update entity def id`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update entity def name`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update entity def description`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update entity def identifier attribute`() {
        TODO("Not yet implemented")
    }
    @Test
    fun `update entity def documentation home attribute`() {
        TODO("Not yet implemented")
    }
    @Test
    fun `update entity origin`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `delete entity def`() {
        TODO("Not yet implemented")
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    @Test
    fun `create attribute def`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update attribute def id`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update attribute def name`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update attribute def description`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update attribute def type`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `update attribute def optional`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `delete attribute def`() {
        TODO("Not yet implemented")
    }

}

