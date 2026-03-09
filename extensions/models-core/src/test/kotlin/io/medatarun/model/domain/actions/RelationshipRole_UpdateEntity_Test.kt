package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityNotFoundException
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.modelRef
import io.medatarun.model.domain.typeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

        val reloadedRelationship = env.query.findModel(env.modelRef).findRelationship(env.relationshipRef)
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
        val otherModelRef = modelRef(otherModelKey)
        val otherEntityRef = EntityRef.ByKey(EntityKey("other-entity"))
        env.dispatch(
            ModelAction.Model_Create(
                modelKey = otherModelKey,
                name = LocalizedTextNotLocalized("Other model"),
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
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
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
