package io.medatarun.ext.db

import kotlinx.serialization.Serializable

@Serializable
data class DriverJson(
    val id: String,
    val name: String,
    val jar: String,
    val className: String
)