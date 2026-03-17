package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Entity_UpdateDocumentationHome_Test {

    @Test
    fun `update entity documentation home not null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.dispatch(ModelAction.Entity_UpdateDocumentationHome(env.modelRef, env.primaryEntityRef, url.toString()))
        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertEquals(url, reloaded.documentationHome)
    }

    @Test
    fun `update entity documentation home to null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvEntityUpdate()
        val url = URI("http://localhost").toURL()
        env.dispatch(ModelAction.Entity_UpdateDocumentationHome(env.modelRef, env.primaryEntityRef, url.toString()))
        env.dispatch(ModelAction.Entity_UpdateDocumentationHome(env.modelRef, env.primaryEntityRef, null))
        val reloaded = env.query.findEntity(env.modelRef, env.primaryEntityRef)
        assertNull(reloaded.documentationHome)
    }
}