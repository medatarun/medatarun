package io.medatarun.auth.actions

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class ActorGet_Test {
    @Test
    fun actorGet() {
        val env = AuthEnvTest()
        env.asAdmin()

        env.dispatch(
            AuthAction.UserCreate(
                username = Username("jane.doe"),
                password = PasswordClear("jane.doe.0123456789"),
                fullname = Fullname("Jane Doe"),
                admin = false
            )
        )

        val actor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            "jane.doe"
        )
        assertNotNull(actor)

        val actorInfo: ActorDetailDto = env.dispatch(AuthAction.ActorGet(actor.id))
        assertEquals(actor.id.value.toString(), actorInfo.id)
        assertEquals("jane.doe", actorInfo.subject)
        assertEquals("Jane Doe", actorInfo.fullname)
        assertEquals(env.oidcService.oidcIssuer(), actorInfo.issuer)
    }

}