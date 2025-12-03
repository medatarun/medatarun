package io.medatarun.ext.db.model

/**
 * Holds a database connection secret
 */
data class DbConnectionSecret(
    /**
     * RAW is the only supported value yet
     */
    val storage: String,
    /**
     * Value is yet the plain text secret
     */
    val value: String
)