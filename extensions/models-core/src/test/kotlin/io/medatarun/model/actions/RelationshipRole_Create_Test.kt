package io.medatarun.model.actions

import io.medatarun.model.domain.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class RelationshipRole_Create_Test {

    @Test
    fun `create relationship role is persisted`() {
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

        val reloaded = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef)
        assertNotNull(reloaded.roles.firstOrNull { role -> role.key == roleKey })
    }

    @Test
    fun `create relationship role with duplicate key then error`() {
        val env = TestEnvRelationshipRole()
        assertFailsWith<RelationshipRoleCreateDuplicateKeyException> {
            env.dispatch(
                ModelAction.RelationshipRole_Create(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    roleKey = env.roleAKey,
                    roleEntityRef = env.primaryEntityRef,
                    roleName = null,
                    roleCardinality = io.medatarun.model.domain.RelationshipCardinality.One
                )
            )
        }
    }

    @Test
    fun `create relationship role with unknown entity then error`() {
        val env = TestEnvRelationshipRole()
        val roleKey = RelationshipRoleKey("observer")
        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_Create(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    roleKey = roleKey,
                    roleEntityRef = EntityRef.ByKey(EntityKey("unknown-entity")),
                    roleName = null,
                    roleCardinality = io.medatarun.model.domain.RelationshipCardinality.Many
                )
            )
        }
    }
}
