package io.medatarun.tags.core.fixtures

import io.medatarun.tags.core.TagTestIllegalStateException
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.ports.needs.TagScopeManager


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

val vehicleScopeType = TagScopeType("vehicle")
fun vehicleScopeRef(vehicleId: SampleId): TagScopeRef.Local {
    return TagScopeRef.Local(vehicleScopeType, TagScopeId(vehicleId.value))
}



/**
 * In-memory test fixture service for vehicle-related objects.
 * This behaves like a tiny repository set used by unit tests.
 */
class VehicleService(
    val onBeforeDelete: (id: SampleId) -> Unit
) {
    private val vehicles = mutableMapOf<SampleId, Vehicle>()
    private val vehicleParts = mutableMapOf<SampleId, VehiclePart>()

    fun createVehicle(item: Vehicle) {
        vehicles[item.id] = item
    }

    fun deleteehicle(id: SampleId) {
        onBeforeDelete(id)
        vehicles.remove(id)
    }

    fun createVehiclePart(item: VehiclePart) {
        vehicleParts[item.id] = item
    }

    fun findVehicleById(id: SampleId): Vehicle {
        return vehicles[id] ?: throw TagTestIllegalStateException("Vehicle not found: $id")
    }

    fun findVehiclePartById(id: SampleId): VehiclePart {
        return vehicleParts[id] ?: throw TagTestIllegalStateException("VehiclePart not found: $id")
    }

    fun removeTagEverywhere(tagId: TagId) {
        vehicles.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
        vehicleParts.replaceAll { _, item -> item.copy(tags = item.tags.filter { it != tagId }) }
    }

    fun existsVehicleById(id: SampleId): Boolean {
        return vehicles[id] != null
    }
}

class VehicleTagScopeManager(
    private val vehicleService: VehicleService
) : TagScopeManager {
    override val type: TagScopeType = vehicleScopeType

    override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
        val id = SampleId(scopeRef.scopeId.value)
        return vehicleService.existsVehicleById(id)
    }


}