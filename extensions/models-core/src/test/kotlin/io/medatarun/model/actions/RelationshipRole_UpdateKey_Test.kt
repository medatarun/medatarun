package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.domain.RelationshipRoleUpdateDuplicateKeyException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@EnableDatabaseTests
class RelationshipRole_UpdateKey_Test {

    @Test
    fun `update relationship role key persisted`() {
        val env = TestEnvRelationshipRole()
        val newRoleKey = RelationshipRoleKey("buyer-updated")
        val updatedRoleId = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.key == env.roleAKey }
            .id
        env.dispatch(
            ModelAction.RelationshipRole_UpdateKey(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = newRoleKey
            )
        )

        val reloaded = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef).roles
        assertEquals(newRoleKey, reloaded.first { role -> role.id == updatedRoleId }.key)
    }

    @Test
    fun `update relationship role key with duplicate key then error`() {
        val env = TestEnvRelationshipRole()
        assertFailsWith<RelationshipRoleUpdateDuplicateKeyException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateKey(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = env.roleARef,
                    value = env.roleBKey
                )
            )
        }
    }
}
