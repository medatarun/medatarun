package io.medatarun.ext.db

import kotlinx.serialization.Serializable

@Serializable
data class DriversJson(
    val drivers: List<DriverJson>
)