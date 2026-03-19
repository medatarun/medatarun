package io.medatarun.model.actions

import io.medatarun.model.domain.RelationshipRoleDeleteMinimumRolesException
import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.domain.RelationshipRoleNotFoundException
import io.medatarun.model.domain.RelationshipRoleRef
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RelationshipRole_Delete_Test {

    @Test
    fun `delete relationship role removes role from relationship`() {
        val env = TestEnvRelationshipRole()
        val roleKey = RelationshipRoleKey("observer")
        env.dispatch(
            ModelAction.RelationshipRole_Create(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                roleKey = roleKey,
                roleEntityRef = env.primaryEntityRef,
                roleName = null,
                roleCardinality = io.medatarun.model.domain.RelationshipCardinality.Many
            )
        )
        env.dispatch(
            ModelAction.RelationshipRole_Delete(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = RelationshipRoleRef.ByKey(roleKey)
            )
        )
        val reloaded = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef)
        assertNull(reloaded.roles.firstOrNull { role -> role.key == roleKey })
    }

    @Test
    fun `delete relationship role by id removes role from relationship`() {
        val env = TestEnvRelationshipRole()
        val roleKey = RelationshipRoleKey("auditor")
        env.dispatch(
            ModelAction.RelationshipRole_Create(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                roleKey = roleKey,
                roleEntityRef = env.primaryEntityRef,
                roleName = null,
                roleCardinality = io.medatarun.model.domain.RelationshipCardinality.Many
            )
        )
        val roleId = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.key == roleKey }
            .id
        env.dispatch(
            ModelAction.RelationshipRole_Delete(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = RelationshipRoleRef.ById(roleId)
            )
        )
        val reloaded = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef)
        assertNull(reloaded.roles.firstOrNull { role -> role.id == roleId })
    }

    @Test
    fun `delete relationship role with unknown role then error`() {
        val env = TestEnvRelationshipRole()
        val roleKey = RelationshipRoleKey("observer")
        env.dispatch(
            ModelAction.RelationshipRole_Create(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                roleKey = roleKey,
                roleEntityRef = env.primaryEntityRef,
                roleName = null,
                roleCardinality = io.medatarun.model.domain.RelationshipCardinality.Many
            )
        )
        assertFailsWith<RelationshipRoleNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_Delete(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = RelationshipRoleRef.ByKey(RelationshipRoleKey("unknown-role"))
                )
            )
        }
    }

    @Test
    fun `delete relationship role when relationship would keep less than two roles then error`() {
        val env = TestEnvRelationshipRole()
        assertFailsWith<RelationshipRoleDeleteMinimumRolesException> {
            env.dispatch(
                ModelAction.RelationshipRole_Delete(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = env.roleARef
                )
            )
        }
    }
}
