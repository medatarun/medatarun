package io.medatarun.auth.actions

import io.medatarun.auth.domain.UsernameEmptyException
import io.medatarun.auth.fixtures.AuthActionEnvTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AuthActionsTest {

    @Test
    fun `bootstrap called`() {
        val env = AuthActionEnvTest(createAdmin = false)
        val token = env.dispatch(
            AuthAction.AdminBootstrap(
                secret = env.env.bootstrapSecretKeeper,
                username = "admin",
                password = "admin.0123456789",
                fullname = "Admin"
            )
        )
        env.env.verifyToken(token.accessToken, expectedSub = "admin")
    }

    @Test
    fun `bootstrap validates username`() {
        val env = AuthActionEnvTest(createAdmin = false)
        assertThrows<UsernameEmptyException> {
            env.dispatch(
                AuthAction.AdminBootstrap(
                    secret = env.env.bootstrapSecretKeeper,
                    username = "",
                    password = "admin.0123456789",
                    fullname = "Admin"
                )
            )
        }
    }


    @Test
    fun `bootstrap validates fullname`() {
        val env = AuthActionEnvTest(createAdmin = false)
        assertThrows<UsernameEmptyException> {
            env.dispatch(
                AuthAction.AdminBootstrap(
                    secret = env.env.bootstrapSecretKeeper,
                    username = "",
                    password = "admin.0123456789",
                    fullname = "Admin"
                )
            )
        }
    }
}