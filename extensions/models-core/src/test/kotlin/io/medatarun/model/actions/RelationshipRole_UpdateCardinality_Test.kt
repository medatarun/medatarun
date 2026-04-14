package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.RelationshipCardinality
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class RelationshipRole_UpdateCardinality_Test {

    @Test
    fun `update relationship role cardinality persisted`() {
        val env = TestEnvRelationshipRole()
        env.dispatch(
            ModelAction.RelationshipRole_UpdateCardinality(
                modelRef = env.modelRef,
                relationshipRef = env.relationshipRef,
                relationshipRoleRef = env.roleARef,
                value = RelationshipCardinality.Many
            )
        )
        val reloadedRelationship = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef)
        val reloadedRole = reloadedRelationship.roles.first { role -> role.key == env.roleAKey }
        assertEquals(RelationshipCardinality.Many, reloadedRole.cardinality)
    }

    @Test
    fun `update relationship role cardinality with all enum values persisted`() {
        val env = TestEnvRelationshipRole()
        for (cardinality in RelationshipCardinality.entries) {
            env.dispatch(
                ModelAction.RelationshipRole_UpdateCardinality(
                    modelRef = env.modelRef,
                    relationshipRef = env.relationshipRef,
                    relationshipRoleRef = env.roleARef,
                    value = cardinality
                )
            )
            val reloadedRelationship = env.query.findModelAggregate(env.modelRef).findRelationship(env.relationshipRef)
            val reloadedRole = reloadedRelationship.roles.first { role -> role.key == env.roleAKey }
            assertEquals(cardinality, reloadedRole.cardinality)
        }
    }
}
