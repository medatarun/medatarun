package io.medatarun.auth.domain

import java.time.Instant
import java.util.*

data class User(
    val id: UUID,
    val login: Username,
    val fullname: Fullname,
    val passwordHash: PasswordHash,
    val admin: Boolean,
    val bootstrap: Boolean,
    val disabledDate: Instant?
) {

}