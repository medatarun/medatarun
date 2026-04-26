package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.UserDisableSelfException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.lang.http.StatusCode
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class User_Enable_Disable_Test {

    @Test
    fun `disable user called`() {
        val env = AuthEnvTest()
        env.createJohn()

        env.asAdmin()
        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.User_Disable(env.johnUsername))

        env.logout()
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(env.johnUsername, env.johnPassword))
        }

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(env.adminUsername, env.adminPassword))
        }

        // Makes sure this propagates to actors
        val actorDisabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), env.johnUsername.value)
        assertTrue(actorDisabled?.disabledDate != null)

    }

    @Test
    fun `admin can not disable himself`() {
        val env = AuthEnvTest()
        env.asAdmin()
        assertThrows<UserDisableSelfException> {
            env.dispatch(AuthAction.User_Disable(env.adminUsername))
        }
    }

    @Test
    fun `another admin can not disable himself`() {
        val env = AuthEnvTest()
        env.asAdmin()
        env.dispatch(
            AuthAction.User_Create(
                username = Username("admin2"),
                fullname = Fullname("Admin2"),
                password = PasswordClear("admin2." + UuidUtils.generateV4String()),
                admin = true
            )
        )
        env.asUser(Username("admin2"))
        assertThrows<UserDisableSelfException> {
            env.dispatch(AuthAction.User_Disable(Username("admin2")))
        }
    }

    @Test
    fun `admin can disable anothe admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        env.dispatch(
            AuthAction.User_Create(
                username = Username("admin2"),
                fullname = Fullname("Admin2"),
                password = PasswordClear("admin2." + UuidUtils.generateV4String()),
                admin = true
            )
        )
        env.asUser(env.adminUsername)
        env.dispatch(AuthAction.User_Disable(Username("admin2")))
        val otherAdmin = env.userService.findByUsername(Username("admin2"))
        assertNotNull(otherAdmin.disabledDate)

    }

    @Test
    fun `admin cal enable user`() {
        val env = AuthEnvTest()
        env.createJohn()
        env.asAdmin()
        env.dispatch(AuthAction.User_Disable(env.johnUsername))

        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.User_Enable(env.johnUsername))

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(env.johnUsername, env.johnPassword))
        }

        // Makes sure this propagates to actors
        val actorEnabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), env.johnUsername.value)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

    @Test
    fun `user can not enbale itself`() {
        val env = AuthEnvTest()
        env.createJohn()
        env.asUser(env.johnUsername)
        val e = assertThrows<ActionInvocationException> {
        env.dispatch(AuthAction.User_Enable(env.johnUsername))
        }
        assertEquals(StatusCode.FORBIDDEN, e.status)
    }


}