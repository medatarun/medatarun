package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.LocalizedTextNotLocalized
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class RelationshipRole_UpdateName_Test {

    @Test
    fun `update relationship role name not null persisted`() {
        val env = TestEnvRelationshipRole()
        val newName = LocalizedTextNotLocalized("Buyer updated")
        env.dispatch(
            ModelAction.RelationshipRole_UpdateName(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = newName
            )
        )

        val reloadedRelationship = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef)
        val reloadedRole = reloadedRelationship.roles.first { role -> role.key == env.roleAKey }
        assertEquals(newName, reloadedRole.name)
    }

    @Test
    fun `update relationship role name null then name is null`() {
        val env = TestEnvRelationshipRole()
        env.dispatch(
            ModelAction.RelationshipRole_UpdateName(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = LocalizedTextNotLocalized("Buyer updated")
            )
        )
        env.dispatch(
            ModelAction.RelationshipRole_UpdateName(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = null
            )
        )

        val reloadedRelationship = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef)
        val reloadedRole = reloadedRelationship.roles.first { role -> role.key == env.roleAKey }
        assertNull(reloadedRole.name)
    }
}
