package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.RelationshipRef.Companion.relationshipRefKey
import io.medatarun.model.domain.RelationshipRoleRef.Companion.roleRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_Delete_Test {

    @Test
    fun `delete entity in model then entity removed`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-delete")
        val entityRef = entityRefKey("entity-to-delete")
        val typeRef = typeRefKey("String")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, LocalizedText("To delete"))
        env.dispatch(ModelAction.Entity_Delete(modelRef, entityRef))

        env.replayWithRebuild {
            assertNull(env.queries.findEntityOptional(modelRef, entityRef))
        }
    }

    @Test
    fun `delete entity with same key in two models then only entity in the specified model is removed`() {
        val env = ModelTestEnv()
        val modelRef1 = modelRefKey("model-1")
        val modelRef2 = modelRefKey("model-2")
        val entityRef = entityRefKey("shared-entity")
        val typeRef = typeRefKey("String")

        env.modelCreate(modelRef1.key)
        env.typeCreate(modelRef1, typeRef.key)
        env.modelCreate(modelRef2.key)
        env.typeCreate(modelRef2, typeRef.key)
        env.entityCreate(modelRef1, entityRef.key, LocalizedText("Entity"))
        env.entityCreate(modelRef2, entityRef.key, LocalizedText("Entity"))
        env.dispatch(ModelAction.Entity_Delete(modelRef1, entityRef))

        assertNull(env.queries.findEntityOptional(modelRef1, entityRef))
        val reloadedModel2Entity = env.queries.findEntity(modelRef2, entityRef)
        assertEquals(entityRef.key, reloadedModel2Entity.key)
    }
    @Test
    fun `delete entity used in relationship roles fails`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("mymodel")
        val typeRef = typeRefKey("id")
        val entityRefRecipe = entityRefKey("recipe")
        val attrRefRecipeIdentifier = entityAttributeRefKey("id")
        val entityRefIngredient = entityRefKey("ingredient")
        val attrRefIngredientIdentifier = entityAttributeRefKey("id")
        val relationshipRef = relationshipRefKey("ingredient_used_recipe")
        val roleIngredientRef = roleRefKey("ingredient")
        val roleRecipeRef = roleRefKey("recipe")
        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRefIngredient.key)
        env.entityAttributeCreate(modelRef, entityRefIngredient, attrRefIngredientIdentifier.key, typeRef)
        env.entityCreate(modelRef, entityRefRecipe.key)
        env.entityAttributeCreate(modelRef, entityRefRecipe, attrRefRecipeIdentifier.key, typeRef)

        env.relationshipCreate(
            modelRef = modelRef,
            relationshipKey = relationshipRef.key,
            roleAKey = roleIngredientRef.key,
            roleAEntityRef = entityRefIngredient,
            roleACardinality = RelationshipCardinality.One,
            roleBKey = roleRecipeRef.key,
            roleBEntityRef = entityRefRecipe,
            roleBCardinality = RelationshipCardinality.One,
        )

        assertFailsWith<EntityDeleteInRelationshipException> {
            env.dispatch(ModelAction.Entity_Delete(modelRef, entityRefRecipe))
        }

    }
}
