package io.medatarun.auth.actions

import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@EnableDatabaseTests
class User_ChangePassword_Test {

    @Test
    fun `change user password called`() {
        val env = AuthEnvTest()
        val passwordNext = PasswordClear("john.doe.987654321")
        env.createJohn()
        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.User_ChangePassword(env.johnUsername, passwordNext))

        env.logout()

        // Allow testing that we didn't invert passwords
        // Old password shall not work again
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(env.johnUsername, env.johnPassword))
        }
        assertDoesNotThrow {
            // New password works
            val token = env.dispatch(AuthAction.Login(env.johnUsername, passwordNext))
            env.verifyToken(token.accessToken, expectedSub = env.johnUsername.value)
            // and that we didn't broke admin
            val tokenAdmin = env.dispatch(AuthAction.Login(env.adminUsername, env.adminPassword))
            env.verifyToken(tokenAdmin.accessToken, expectedSub = env.adminUsername.value)
        }
    }

}