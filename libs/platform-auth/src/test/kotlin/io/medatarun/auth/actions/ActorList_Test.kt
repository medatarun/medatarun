package io.medatarun.auth.actions

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.fixtures.AuthTestUtils.createActorJwt
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppActorSystemMaintenance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

@EnableDatabaseTests
class ActorList_Test {
    @Test
    fun actorList() {
        val env = AuthEnvTest()
        env.asAdmin()
        env.dispatch(
            AuthAction.UserCreate(
                username = Username("john.doe"),
                password = PasswordClear("john.doe.0123456789"),
                fullname = Fullname("John Doe"),
                admin = false
            )
        )
        env.dispatch(
            AuthAction.UserCreate(
                username = Username("john.doe2"),
                password = PasswordClear("john.doe2.0123456789"),
                fullname = Fullname("John Doe2"),
                admin = false
            )
        )

        env.actorService.syncFromJwtExternalPrincipal(
            createActorJwt(
                "https://microsoft.com/azuread/123456789",
                "sandra.tafroilanuit",
                "Sandra Tafroilanuit",
                "sandra.tafroilanuit@test.azure.local"
            )
        )

        val actors: List<ActorInfoDto> = env.dispatch(AuthAction.ActorList())
        assertEquals(5, actors.size)

        val sysActor = actors.first { actor -> actor.id == AppActorSystemMaintenance.id.asString() }
        assertEquals(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_SUBJECT, sysActor.subject)
        assertEquals(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ISSUER, sysActor.issuer)
        assertEquals(AppActorSystemMaintenance.displayName, sysActor.fullname)
        assertEquals(UuidUtils.getInstant(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID), sysActor.createdAt)
        assertEquals(UuidUtils.getInstant(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID), sysActor.lastSeenAt)

        val adminActor = actors.first { actor -> actor.subject == env.adminUsername.value }
        assertEquals(env.adminUsername.value, adminActor.subject)
        assertEquals(env.adminFullname.value, adminActor.fullname)
        assertEquals(env.oidcService.oidcIssuer(), adminActor.issuer)
        assertDoesNotThrow { UuidUtils.fromString(adminActor.id) }
        assertEquals(
            env.authClockTests.staticNow.truncatedTo(ChronoUnit.MILLIS),
            adminActor.createdAt.truncatedTo(ChronoUnit.MILLIS)
        )
        assertEquals(
            env.authClockTests.staticNow.truncatedTo(ChronoUnit.MILLIS),
            adminActor.lastSeenAt.truncatedTo(ChronoUnit.MILLIS)
        )

        val johnActor = actors.first { actor -> actor.subject == Username("john.doe").value }
        assertEquals("john.doe", johnActor.subject)
        assertEquals("John Doe", johnActor.fullname)
        assertEquals(env.oidcService.oidcIssuer(), johnActor.issuer)

        val john2Actor = actors.first { actor -> actor.subject == Username("john.doe2").value }
        assertEquals("john.doe2", john2Actor.subject)
        assertEquals("John Doe2", john2Actor.fullname)
        assertEquals(env.oidcService.oidcIssuer(), john2Actor.issuer)

        val sandraActor = actors.first { actor -> actor.subject == Username("sandra.tafroilanuit").value }
        assertEquals("sandra.tafroilanuit", sandraActor.subject)
        assertEquals("Sandra Tafroilanuit", sandraActor.fullname)
        assertEquals("https://microsoft.com/azuread/123456789", sandraActor.issuer)

    }

}