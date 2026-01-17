package io.medatarun.ext.db.internal.connection

import kotlinx.serialization.Serializable

@Serializable
data class DbDatasourcesJson(
    val datasources: List<DbConnectionJson>
)