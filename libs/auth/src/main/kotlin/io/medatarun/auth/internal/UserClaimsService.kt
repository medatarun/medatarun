package io.medatarun.auth.internal

import io.medatarun.auth.domain.User

class UserClaimsService {

    fun createUserClaims(user: User): Map<String, String> = mapOf(
        "name" to user.fullname,
        "role" to (if (user.admin) "admin" else ""),
        "mid" to user.id.toString()
    )

}