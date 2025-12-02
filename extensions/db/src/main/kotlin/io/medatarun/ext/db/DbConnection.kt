package io.medatarun.ext.db

data class DbConnection(
    val name: String,
    val driver: String,
    val url: String,
)