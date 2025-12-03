package io.medatarun.ext.db.internal.connection

import kotlinx.serialization.Serializable

@Serializable
data class DbConnectionsJson(
    val connections: List<DbConnectionJson>
)