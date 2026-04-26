package io.medatarun.auth.actions

import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@EnableDatabaseTests
class User_ChangeMyPassword_Test {

    @Test
    fun `change own password called`() {
        val env = AuthEnvTest()
        val passwordNext = PasswordClear("john.doe.." + UuidUtils.generateV4String())
        env.createJohn()
        env.asUser(env.johnUsername)
        @Suppress("UnusedVariable", "unused") val result: Unit =
            env.dispatch(AuthAction.User_ChangeMyPassword(env.johnPassword, passwordNext))

        // Allow testing that we didn't invert passwords
        // Old password shall not work again
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(env.johnUsername, env.johnPassword))
        }

        // New password works
        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(env.johnUsername, passwordNext))
        }

        // Didn't change by mistake admin password
        env.logout()
        val token = env.dispatch(AuthAction.Login(env.adminUsername, env.adminPassword))
        val decoded = env.verifyToken(token.accessToken, expectedSub = env.adminUsername.value)
        assertEquals(env.adminUsername.value, decoded.subject)

    }

}