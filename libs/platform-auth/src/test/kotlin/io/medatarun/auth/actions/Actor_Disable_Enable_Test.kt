package io.medatarun.auth.actions

import io.medatarun.auth.domain.AuthNotAuthenticatedException
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.fixtures.AuthTestUtils.createJwtExternalPrincipal
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class Actor_Disable_Enable_Test {
    @Test
    fun `actor disable uses actor service for external issuer`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val issuer = "https://example.com/oidc"
        val subject = "external.user"
        env.actorService.syncFromJwtExternalPrincipal(createJwtExternalPrincipal(issuer, subject))
        val actor = env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actor)

        env.authClockTests.staticNow = env.authClockTests.now().plusSeconds(60*10)

        env.dispatch(AuthAction.Actor_Disable(actor.id))

        val actorDisabled = env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actorDisabled)
        assertEquals(env.authClockTests.staticNow, actorDisabled.disabledDate)

        env.dispatch(AuthAction.Actor_Enable(actor.id))

        val actorEnabled = env.actorService.findByIssuerAndSubjectOptional(issuer, subject)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

    @Test
    fun `actor disable uses user service for internal issuer`() {
        val env = AuthEnvTest()
        val username = Username("jane.doe")
        val password = PasswordClear("jane.doe.0123456789")
        val fullname = Fullname("Jane Doe")
        env.asAdmin()
        env.dispatch(
            AuthAction.User_Create(
                username = username,
                password = password,
                fullname = fullname,
                admin = false
            )
        )

        val actor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            username.value
        )
        assertNotNull(actor)

        env.dispatch(AuthAction.Actor_Disable(actor.id))

        env.logout()
        assertThrows<AuthNotAuthenticatedException> {
            env.dispatch(AuthAction.Login(username, password))
        }

        val actorDisabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertTrue(actorDisabled?.disabledDate != null)

        env.asAdmin()
        env.dispatch(AuthAction.Actor_Enable(actor.id))
        env.logout()

        assertDoesNotThrow {
            env.dispatch(AuthAction.Login(username, password))
        }

        val actorEnabled =
            env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), username.value)
        assertNotNull(actorEnabled)
        assertEquals(null, actorEnabled.disabledDate)
    }

}