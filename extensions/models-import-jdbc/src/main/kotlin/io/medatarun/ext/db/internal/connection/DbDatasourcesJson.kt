package io.medatarun.ext.db.internal.connection

import kotlinx.serialization.Serializable

@Serializable
internal data class DbDatasourcesJson(
    val datasources: List<DbConnectionJson>
)