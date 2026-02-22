package io.medatarun.tags.core.fixtures

/**
 * In-memory test fixture service for recipe-related objects.
 * This behaves like a tiny repository set used by unit tests.
 */
class RecipeService {
    private val recipes = mutableMapOf<SampleId, Recipe>()
    private val ingredients = mutableMapOf<SampleId, Ingredient>()
    private val recipeSteps = mutableMapOf<SampleId, RecipeStep>()

    fun createRecipe(item: Recipe) {
        recipes[item.id] = item
    }

    fun createIngredient(item: Ingredient) {
        ingredients[item.id] = item
    }

    fun createRecipeStep(item: RecipeStep) {
        recipeSteps[item.id] = item
    }

    fun findRecipeById(id: SampleId): Recipe {
        return recipes[id] ?: throw IllegalStateException("Recipe not found: $id")
    }

    fun findIngredientById(id: SampleId): Ingredient {
        return ingredients[id] ?: throw IllegalStateException("Ingredient not found: $id")
    }

    fun findRecipeStepById(id: SampleId): RecipeStep {
        return recipeSteps[id] ?: throw IllegalStateException("RecipeStep not found: $id")
    }

    fun removeTagEverywhere(tagId: io.medatarun.tags.core.domain.TagId) {
        recipes.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
        ingredients.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
        recipeSteps.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
    }
}

/**
 * In-memory test fixture service for vehicle-related objects.
 * This behaves like a tiny repository set used by unit tests.
 */
class VehicleService {
    private val vehicles = mutableMapOf<SampleId, Vehicle>()
    private val vehicleParts = mutableMapOf<SampleId, VehiclePart>()

    fun createVehicle(item: Vehicle) {
        vehicles[item.id] = item
    }

    fun createVehiclePart(item: VehiclePart) {
        vehicleParts[item.id] = item
    }

    fun findVehicleById(id: SampleId): Vehicle {
        return vehicles[id] ?: throw IllegalStateException("Vehicle not found: $id")
    }

    fun findVehiclePartById(id: SampleId): VehiclePart {
        return vehicleParts[id] ?: throw IllegalStateException("VehiclePart not found: $id")
    }

    fun removeTagEverywhere(tagId: io.medatarun.tags.core.domain.TagId) {
        vehicles.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
        vehicleParts.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
    }
}
