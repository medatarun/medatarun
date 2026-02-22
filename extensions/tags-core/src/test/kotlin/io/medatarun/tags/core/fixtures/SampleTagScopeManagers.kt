package io.medatarun.tags.core.fixtures

import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.ports.needs.TagScopeManager
import java.util.UUID

class RecipeTagScopeManager(
    private val recipeService: RecipeService
) : TagScopeManager {
    override val type: TagScopeType = SampleScopes.recipeScopeType

    override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
        val id = scopeRef.scopeId.value
        return recipeService.existsRecipeById(SampleId(id))
    }

    override fun onBeforeTagDelete(tagId: TagId) {
        recipeService.removeTagEverywhere(tagId)
    }
}

class VehicleTagScopeManager(
    private val vehicleService: VehicleService
) : TagScopeManager {
    override val type: TagScopeType = SampleScopes.vehicleScopeType

    override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
        val id = SampleId(scopeRef.scopeId.value)
        return vehicleService.existsVehicleById(id)
    }

    override fun onBeforeTagDelete(tagId: TagId) {
        vehicleService.removeTagEverywhere(tagId)
    }
}
