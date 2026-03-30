package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Model_Create_Test {

    @Test
    fun `create model with name description and version when present`() {
        val env = createEnv()
        val query: ModelQueries = env.queries

        val modelKey = ModelKey("m-")
        val name = LocalizedTextNotLocalized("Model name")
        val description = LocalizedMarkdownNotLocalized("Model description")
        val version = ModelVersion("2.0.0")

        env.dispatch(ModelAction.Model_Create(modelKey, name, description, version))

        env.replayWithRebuild {
            val reloaded = query.findModelByKey(modelKey)
            assertEquals(name, reloaded.name)
            assertEquals(description, reloaded.description)
            assertEquals(version, reloaded.version)
            assertEquals(ModelOrigin.Manual, reloaded.origin)
            assertEquals(ModelAuthority.SYSTEM, reloaded.authority)
        }
    }

    @Test
    fun `create model keeps values without optional description`() {

        val env = createEnv()
        val query: ModelQueries = env.queries

        val modelKey = ModelKey("m")
        val name = LocalizedTextNotLocalized("Model without description")
        val version = ModelVersion("3.0.0")

        env.dispatch(ModelAction.Model_Create(modelKey, name, null, version))

        val found = query.findModelByKey(modelKey)
        assertEquals(name, found.name)
        assertNull(found.description)
        assertEquals(version, found.version)
        assertEquals(ModelOrigin.Manual, found.origin)
        assertEquals(ModelAuthority.SYSTEM, found.authority)

        // Creating a model immediately creates a first version
        env.assertUniqueVersion(version, found.id)

    }
}
