package io.medatarun.ext.db.internal.drivers

import kotlinx.serialization.Serializable

@Serializable
internal data class DriversJson(
    val drivers: List<DriverJson>
)