package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@EnableDatabaseTests
class RelationshipRole_UpdateX_Test {

    @Test
    fun `update relationship role using relationship role ref by key resolved`() {
        val env = TestEnvRelationshipRole()
        val newName = LocalizedText("Buyer by key")
        env.dispatch(
            ModelAction.RelationshipRole_UpdateName(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = newName
            )
        )

        val reloaded = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.key == env.roleAKey }
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update relationship role using relationship role ref by id resolved`() {
        val env = TestEnvRelationshipRole()
        val roleId = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.key == env.roleAKey }
            .id
        val newName = LocalizedText("Buyer by id")

        env.dispatch(
            ModelAction.RelationshipRole_UpdateName(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = RelationshipRoleRef.ById(roleId),
                value = newName
            )
        )

        val reloaded = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.id == roleId }
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update relationship role with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvRelationshipRole()
        assertFailsWith<ModelNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateName(
                    modelRef = ModelRef.modelRefKey(ModelKey("unknown-model")),
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = env.roleARef,
                    value = LocalizedText("Ignored")
                )
            )
        }
    }

    @Test
    fun `update relationship role with wrong relationship id throws RelationshipNotFoundException`() {
        val env = TestEnvRelationshipRole()
        assertFailsWith<RelationshipNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateName(
                    modelRef = env.modelRef,
                    relationshipRef = RelationshipRef.ByKey(RelationshipKey("unknown-relationship")),
                    relationshipRoleRef = env.roleARef,
                    value = LocalizedText("Ignored")
                )
            )
        }
    }

    @Test
    fun `update relationship role with wrong role id throws RelationshipRoleNotFoundException`() {
        val env = TestEnvRelationshipRole()
        assertFailsWith<RelationshipRoleNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateName(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = RelationshipRoleRef.ByKey(RelationshipRoleKey("unknown-role")),
                    value = LocalizedText("Ignored")
                )
            )
        }
    }
}
