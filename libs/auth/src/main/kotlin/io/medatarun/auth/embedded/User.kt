package io.medatarun.auth.embedded

import java.time.Instant
import java.util.*


data class User(
    val id: UUID,
    val login: String,
    val fullname: String,
    val passwordHash: String,
    val admin: Boolean,
    val bootstrap: Boolean,
    val disabledDate: Instant?
)
