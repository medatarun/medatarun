package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

        val reloadedRelationship = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef)
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

        val reloadedRelationship = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef)
        val reloadedRole = reloadedRelationship.roles.first { role -> role.key == env.roleAKey }
        assertNull(reloadedRole.name)
    }
}
