package io.medatarun.tags.core.fixtures

import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.ports.needs.TagScopeManager

class RecipeTagScopeManager(
    private val recipeService: RecipeService
) : TagScopeManager {
    override val type: TagScopeType = SampleScopes.recipeScopeType

    override fun onBeforeTagDelete(tagId: TagId) {
        recipeService.removeTagEverywhere(tagId)
    }
}

class VehicleTagScopeManager(
    private val vehicleService: VehicleService
) : TagScopeManager {
    override val type: TagScopeType = SampleScopes.vehicleScopeType

    override fun onBeforeTagDelete(tagId: TagId) {
        vehicleService.removeTagEverywhere(tagId)
    }
}
