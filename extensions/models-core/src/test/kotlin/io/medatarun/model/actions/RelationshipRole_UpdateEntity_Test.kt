package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.TypeKey
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@EnableDatabaseTests
class RelationshipRole_UpdateEntity_Test {

    @Test
    fun `update relationship role entity persisted`() {
        val env = TestEnvRelationshipRole()
        val targetEntityId = env.query.findEntity(env.modelRef, env.secondaryEntityRef).id

        env.dispatch(
            ModelAction.RelationshipRole_UpdateEntity(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = env.secondaryEntityRef
            )
        )

        val reloadedRelationship = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef)
        val reloadedRole = reloadedRelationship.roles.first { role -> role.key == env.roleAKey }
        assertEquals(targetEntityId, reloadedRole.entityId)
    }

    @Test
    fun `update relationship role entity with unknown entity then error`() {
        val env = TestEnvRelationshipRole()
        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateEntity(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = env.roleARef,
                    value = EntityRef.ByKey(EntityKey("unknown-entity"))
                )
            )
        }
    }

    @Test
    fun `update relationship role entity with entity from another model then error`() {
        val env = TestEnvRelationshipRole()

        val otherModelKey = ModelKey("other-model")
        val otherModelRef = ModelRef.modelRefKey(otherModelKey)
        val otherEntityRef = EntityRef.ByKey(EntityKey("other-entity"))
        env.dispatch(
            ModelAction.Model_Create(
                key = otherModelKey,
                name = LocalizedText("Other model"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        env.dispatch(ModelAction.Type_Create(otherModelRef, TypeKey("String"), null, null))
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = otherModelRef,
                entityKey = EntityKey("other-entity"),
                name = null,
                description = null,
                documentationHome = null
            )
        )

        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateEntity(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = env.roleARef,
                    value = otherEntityRef
                )
            )
        }
    }
}
