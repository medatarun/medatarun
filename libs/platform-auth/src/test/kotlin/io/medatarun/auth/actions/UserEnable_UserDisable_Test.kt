package io.medatarun.auth.actions

import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class UserEnable_UserDisable_Test {

    @Test
    fun `disable user called`() {
        val env = AuthEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
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

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserDisable(username))

        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(username, password))
        }

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(env.adminUsername, env.adminPassword))
        }

        // Makes sure this propagates to actors
        val actorDisabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertTrue(actorDisabled?.disabledDate != null)

    }

    @Test
    fun `enable user called`() {
        val env = AuthEnvTest()
        val username = Username("john.doe")
        val password = PasswordClear("john.doe.0123456789")
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
        env.dispatch(AuthAction.UserDisable(username))

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.UserEnable(username))

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, password))
        }

        // Makes sure this propagates to actors
        val actorEnabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }


}