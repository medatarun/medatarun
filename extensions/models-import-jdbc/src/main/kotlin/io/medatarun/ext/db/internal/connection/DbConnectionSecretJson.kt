package io.medatarun.ext.db.internal.connection

import kotlinx.serialization.Serializable

@Serializable
data class DbConnectionSecretJson(
    /**
     * RAW is the only supported value yet
     */
    val storage: String,
    /**
     * Value is yet the plain text secret
     */
    val value: String
)