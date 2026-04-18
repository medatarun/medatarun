package io.medatarun.auth.actions

import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@EnableDatabaseTests
class Login_Test {

    @Test
    fun `can login with ok credentials called`() {
        val env = AuthEnvTest()
        env.createJohn()
        assertDoesNotThrow {
            val token: OAuthTokenResponseDto = env.dispatch(AuthAction.Login(env.johnUsername, env.johnPassword))
            val decoded = env.verifyToken(token.accessToken, expectedSub = env.johnUsername.value)
            assertEquals(env.johnUsername.value, decoded.subject)
        }
    }

    @Test
    fun `john cannot log in with bad login`() {
        val env = AuthEnvTest()
        env.createJohn()
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(
                AuthAction.Login(
                    username = Username(env.johnUsername.value + "--"),
                    password = env.johnPassword
                )
            )

        }
    }

    @Test
    fun `john cannot log in with bad password`() {
        val env = AuthEnvTest()
        env.createJohn()
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(
                AuthAction.Login(
                    username = env.johnUsername,
                    password = PasswordClear(env.johnPassword.value + "---")
                )
            )
        }
    }

    @Test
    fun `john cannot log in with admin password`() {
        val env = AuthEnvTest()
        env.createJohn()
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(
                AuthAction.Login(
                    username = env.johnUsername,
                    password = env.adminPassword
                )
            )

        }
    }

    @Test
    fun `john cannot fake admin with its password`() {
        val env = AuthEnvTest()
        env.createJohn()
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(
                AuthAction.Login(
                    env.adminUsername, env.johnPassword
                )
            )

        }
    }

    @Test
    fun `disabled user can not log in`() {
        val env = AuthEnvTest()
        env.createJohn()
        // Check that john can log in otherwise the test has no sense
        val token = env.dispatch(AuthAction.Login(env.johnUsername, env.johnPassword))
        env.verifyToken(token.accessToken, expectedSub = env.johnUsername.value)
        // disable john
        env.asAdmin()
        env.dispatch(AuthAction.UserDisable(env.johnUsername))
        env.logout()
        // John cannot log in avain
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(env.johnUsername, env.johnPassword))
        }
    }


}