package io.medatarun.ext.db.internal.drivers

import kotlinx.serialization.Serializable

@Serializable
data class DriversJson(
    val drivers: List<DriverJson>
)