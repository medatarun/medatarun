package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.user.*
import java.time.Instant

interface UserStorage {
    fun insert(
        id: UserId,
        login: Username,
        fullname: Fullname,
        password: PasswordHash,
        admin: Boolean,
        bootstrap: Boolean,
        disabledDate: Instant?
    )

    fun findByLogin(login: Username): User?

    fun updatePassword(login: Username, newPassword: PasswordHash)

    fun disable(login: Username, at: Instant = Instant.now())
    fun updateFullname(username: Username, fullname: Fullname)


}