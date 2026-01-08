package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.User
import java.time.Instant

interface UserStore {
    fun insert(
        id: String,
        login: String,
        fullname: String,
        password: String,
        admin: Boolean,
        bootstrap: Boolean,
        disabledDate: Instant?
    )

    fun findByLogin(login: String): User?

    fun updatePassword(login: String, newPassword: String)

    fun disable(login: String, at: Instant = Instant.now())
    fun updateFullname(username: String, fullname: String)


}