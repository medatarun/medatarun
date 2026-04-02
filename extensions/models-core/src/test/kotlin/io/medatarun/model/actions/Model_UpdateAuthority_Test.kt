package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.ModelAuthority
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class Model_UpdateAuthority_Test {

    @Test
    fun `updates on model authority persists the authority`() {
        val env = TestEnvOneModel()
        env.dispatch(ModelAction.Model_UpdateAuthority(env.modelRef, ModelAuthority.CANONICAL))
        assertEquals(ModelAuthority.CANONICAL, env.query.findModel(env.modelRef).authority)
    }
}
