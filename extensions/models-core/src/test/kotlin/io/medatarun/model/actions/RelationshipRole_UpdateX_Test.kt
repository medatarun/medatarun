package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipNotFoundException
import io.medatarun.model.domain.RelationshipRef
import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.domain.RelationshipRoleNotFoundException
import io.medatarun.model.domain.RelationshipRoleRef
import io.medatarun.model.domain.modelRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RelationshipRole_UpdateX_Test {

    @Test
    fun `update relationship role using relationship role ref by key resolved`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvRelationshipRole()
        val newName = LocalizedTextNotLocalized("Buyer by key")
        env.dispatch(
            ModelAction.RelationshipRole_UpdateName(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = newName
            )
        )

        val reloaded = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.key == env.roleAKey }
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update relationship role using relationship role ref by id resolved`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvRelationshipRole()
        val roleId = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.key == env.roleAKey }
            .id
        val newName = LocalizedTextNotLocalized("Buyer by id")

        env.dispatch(
            ModelAction.RelationshipRole_UpdateName(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = RelationshipRoleRef.ById(roleId),
                value = newName
            )
        )

        val reloaded = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef).roles
            .first { role -> role.id == roleId }
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update relationship role with wrong model id throws ModelNotFoundException`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvRelationshipRole()
        assertFailsWith<ModelNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateName(
                    modelRef = modelRef(ModelKey("unknown-model")),
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = env.roleARef,
                    value = LocalizedTextNotLocalized("Ignored")
                )
            )
        }
    }

    @Test
    fun `update relationship role with wrong relationship id throws RelationshipNotFoundException`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvRelationshipRole()
        assertFailsWith<RelationshipNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateName(
                    modelRef = env.modelRef,
                    relationshipRef = RelationshipRef.ByKey(RelationshipKey("unknown-relationship")),
                    relationshipRoleRef = env.roleARef,
                    value = LocalizedTextNotLocalized("Ignored")
                )
            )
        }
    }

    @Test
    fun `update relationship role with wrong role id throws RelationshipRoleNotFoundException`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvRelationshipRole()
        assertFailsWith<RelationshipRoleNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateName(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = RelationshipRoleRef.ByKey(RelationshipRoleKey("unknown-role")),
                    value = LocalizedTextNotLocalized("Ignored")
                )
            )
        }
    }
}
