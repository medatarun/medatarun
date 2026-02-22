package io.medatarun.tags.core

import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.fixtures.Ingredient
import io.medatarun.tags.core.fixtures.Recipe
import io.medatarun.tags.core.fixtures.RecipeStep
import io.medatarun.tags.core.fixtures.SampleId
import io.medatarun.tags.core.fixtures.Vehicle
import io.medatarun.tags.core.fixtures.VehiclePart
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TagEventListenerTest {

    @Test
    fun `tag delete notifies scope managers and removes tag from recipe and vehicle objects`() {
        val env = TagTestEnv()

        env.dispatch(TagAction.TagGroupCreate(io.medatarun.tags.core.domain.TagGroupKey("governance"), null, null))
        val group = env.tagStorage.findTagGroupByKeyOptional(io.medatarun.tags.core.domain.TagGroupKey("governance"))
        assertNotNull(group)

        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), TagKey("shared"), null, null))
        val deletedTag = env.tagStorage.findTagByKeyOptional(group.id, TagKey("shared"))
        assertNotNull(deletedTag)

        env.dispatch(TagAction.TagManagedCreate(TagGroupRef.ById(group.id), TagKey("keep"), null, null))
        val keptTag = env.tagStorage.findTagByKeyOptional(group.id, TagKey("keep"))
        assertNotNull(keptTag)

        val recipeId = sampleId("11111111-2222-3333-4444-555555555555")
        val ingredientId = sampleId("11111111-2222-3333-4444-555555555556")
        val stepId = sampleId("11111111-2222-3333-4444-555555555557")
        val vehicleId = sampleId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1")
        val partId = sampleId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee2")

        env.recipeService.createRecipe(Recipe(recipeId, "Pasta", listOf(deletedTag.id, keptTag.id)))
        env.recipeService.createIngredient(Ingredient(ingredientId, recipeId, "Tomato", listOf(deletedTag.id)))
        env.recipeService.createRecipeStep(RecipeStep(stepId, recipeId, "Boil water", listOf(deletedTag.id, keptTag.id)))

        env.vehicleService.createVehicle(Vehicle(vehicleId, "Truck", listOf(deletedTag.id)))
        env.vehicleService.createVehiclePart(VehiclePart(partId, vehicleId, "Wheel", listOf(keptTag.id, deletedTag.id)))

        env.dispatch(TagAction.TagManagedDelete(io.medatarun.tags.core.domain.TagRef.ById(deletedTag.id)))

        assertEquals(listOf(keptTag.id), env.recipeService.findRecipeById(recipeId).tags)
        assertEquals(emptyList(), env.recipeService.findIngredientById(ingredientId).tags)
        assertEquals(listOf(keptTag.id), env.recipeService.findRecipeStepById(stepId).tags)
        assertEquals(emptyList(), env.vehicleService.findVehicleById(vehicleId).tags)
        assertEquals(listOf(keptTag.id), env.vehicleService.findVehiclePartById(partId).tags)
    }

    private fun sampleId(value: String): SampleId {
        return SampleId(UUID.fromString(value))
    }
}
