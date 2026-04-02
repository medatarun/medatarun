package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class Model_UpdateDocumentationHome_Test {

    @Test
    fun `update documentation home with value then updated`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.dispatch(ModelAction.Model_UpdateDocumentationHome(env.modelRef, url.toString()))
        assertEquals(url, env.query.findModel(env.modelRef).documentationHome)
    }

    @Test
    fun `update documentation home with null then updated to null`() {
        val env = TestEnvOneModel()
        val url = URI("https://some.url/index.html").toURL()
        env.dispatch(ModelAction.Model_UpdateDocumentationHome(env.modelRef, url.toString()))
        env.dispatch(ModelAction.Model_UpdateDocumentationHome(env.modelRef, null))
        assertNull(env.query.findModel(env.modelRef).documentationHome)
    }
}