package io.medatarun.auth.actions

import io.medatarun.auth.domain.Fullname
import io.medatarun.auth.domain.Username
import io.medatarun.auth.fixtures.AuthActionEnvTest
import org.junit.jupiter.api.Test

class AuthActionsTest {

    @Test
    fun `bootstrap called`() {
        val env = AuthActionEnvTest(createAdmin = false)
        val token = env.dispatch(
            AuthAction.AdminBootstrap(
                secret = env.env.bootstrapSecretKeeper,
                username = Username("admin"),
                password = "admin.0123456789",
                fullname = Fullname("Admin")
            )
        )
        env.env.verifyToken(token.accessToken, expectedSub = "admin")
    }
}