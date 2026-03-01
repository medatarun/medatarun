package io.medatarun.tags.core.fixtures

import io.medatarun.tags.core.TagTestIllegalStateException
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.ports.needs.TagScopeManager

/**
 * Recipe is the local scope root for the "food" sample domain.
 * Tags attached to a recipe and its children are expected to use the recipe scope.
 */
data class Recipe(
    val id: SampleId,
    val name: String,
    val tags: List<TagId>,
)

/**
 * Ingredient belongs to a recipe and is taggable in the same recipe scope.
 */
data class Ingredient(
    val id: SampleId,
    val recipeId: SampleId,
    val name: String,
    val tags: List<TagId>,
)

/**
 * RecipeStep belongs to a recipe and is taggable in the same recipe scope.
 */
data class RecipeStep(
    val id: SampleId,
    val recipeId: SampleId,
    val name: String,
    val tags: List<TagId>,
)


val recipeScopeType = TagScopeType("recipe")


fun recipeScopeRef(recipeId: SampleId): TagScopeRef.Local {
    return TagScopeRef.Local(recipeScopeType, TagScopeId(recipeId.value))
}


/**
 * In-memory test fixture service for recipe-related objects.
 * This behaves like a tiny repository set used by unit tests.
 */
class RecipeService(
    val onBeforeDelete:(id: SampleId) -> Unit
) {
    private val recipes = mutableMapOf<SampleId, Recipe>()
    private val ingredients = mutableMapOf<SampleId, Ingredient>()
    private val recipeSteps = mutableMapOf<SampleId, RecipeStep>()

    fun createRecipe(item: Recipe) {
        recipes[item.id] = item
    }

    fun deleteRecipe(id: SampleId) {
        onBeforeDelete(id)
        recipes.remove(id)
    }

    fun createIngredient(item: Ingredient) {
        ingredients[item.id] = item
    }

    fun createRecipeStep(item: RecipeStep) {
        recipeSteps[item.id] = item
    }

    fun findRecipeById(id: SampleId): Recipe {
        return recipes[id] ?: throw TagTestIllegalStateException("Recipe not found: $id")
    }

    fun findIngredientById(id: SampleId): Ingredient {
        return ingredients[id] ?: throw TagTestIllegalStateException("Ingredient not found: $id")
    }

    fun findRecipeStepById(id: SampleId): RecipeStep {
        return recipeSteps[id] ?: throw TagTestIllegalStateException("RecipeStep not found: $id")
    }

    fun removeTagEverywhere(tagId: TagId) {
        recipes.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
        ingredients.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
        recipeSteps.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
    }

    fun existsRecipeById(id: SampleId): Boolean {
        return recipes[id] != null
    }
}


class RecipeTagScopeManager(
    private val recipeService: RecipeService
) : TagScopeManager {
    override val type: TagScopeType = recipeScopeType

    override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
        val id = scopeRef.scopeId.value
        return recipeService.existsRecipeById(SampleId(id))
    }


}

