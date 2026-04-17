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

@EnableDatabaseTests
class UserChangePassword_Test {
    @Test
    fun `change user password called`() {
        val env = AuthEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
        val passwordNext = PasswordClear("john.doe.987654321")
        val fullname = Fullname("John Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )
        env.asAdmin()

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserChangePassword(username, passwordNext))

        env.logout()
        // Allow testing that we didn't invert passwords
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(username, password))
        }
        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, passwordNext))
            // and that we didn't broke admin
            env.dispatch(AuthAction.Login(env.adminUsername, env.adminPassword))
        }
    }

}