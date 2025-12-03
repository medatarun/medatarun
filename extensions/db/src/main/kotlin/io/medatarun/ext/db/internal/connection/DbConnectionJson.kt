package io.medatarun.ext.db.internal.connection

import io.medatarun.ext.db.model.DbConnectionSecret
import kotlinx.serialization.Serializable

@Serializable
data class DbConnectionJson (
    val name: String,
    val driver: String,
    val url: String,
    val username: String,
    val secret: DbConnectionSecretJson,
    val properties: Map<String,String> = emptyMap()
)