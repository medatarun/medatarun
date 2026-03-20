package io.medatarun.model.actions

import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityUpdateKeyDuplicateKeyException
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class Entity_UpdateKey_Test {

    @Test
    fun `update entity key with duplicate key throws exception`() {
        val env = TestEnvEntityUpdate()
        val duplicateId = env.secondaryEntityKey

        assertFailsWith<EntityUpdateKeyDuplicateKeyException> {
            env.dispatch(
                ModelAction.Entity_UpdateKey(
                    env.modelRef,
                    env.primaryEntityRef,
                    duplicateId
                )
            )
        }
    }

    @Test
    fun `update entity key with correct key ok`() {
        val env = TestEnvEntityUpdate()
        val newId = EntityKey("entity-renamed")

        env.dispatch(ModelAction.Entity_UpdateKey(env.modelRef, env.primaryEntityRef, newId))

        val reloaded = env.query.findModel(env.modelRef)
        assertNull(reloaded.findEntityOptional(env.primaryEntityKey))
        assertNotNull(reloaded.findEntityOptional(newId))
    }

}