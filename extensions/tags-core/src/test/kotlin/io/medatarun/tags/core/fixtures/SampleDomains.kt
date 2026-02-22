package io.medatarun.tags.core.fixtures

import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.type.commons.id.Id
import java.util.UUID

/**
 * Single test-only ID type reused by all sample domain objects used in tag assignment tests.
 * We intentionally keep a shared type to reduce fixture noise.
 */
@JvmInline
value class SampleId(override val value: UUID) : Id<SampleId>

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

/**
 * Vehicle is the local scope root for the "vehicle" sample domain.
 */
data class Vehicle(
    val id: SampleId,
    val name: String,
    val tags: List<TagId>,
)

/**
 * VehiclePart belongs to a vehicle and is taggable in the same vehicle scope.
 */
data class VehiclePart(
    val id: SampleId,
    val vehicleId: SampleId,
    val name: String,
    val tags: List<TagId>,
)

object SampleScopes {
    val recipeScopeType = TagScopeType("recipe")
    val vehicleScopeType = TagScopeType("vehicle")

    fun recipeScopeRef(recipeId: SampleId): TagScopeRef.Local {
        return TagScopeRef.Local(recipeScopeType, TagScopeId(recipeId.value))
    }

    fun vehicleScopeRef(vehicleId: SampleId): TagScopeRef.Local {
        return TagScopeRef.Local(vehicleScopeType, TagScopeId(vehicleId.value))
    }
}
