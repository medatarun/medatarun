package io.medatarun.auth.actions

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class Actor_Get_Test {
    @Test
    fun actorGet() {
        val env = AuthEnvTest()
        env.createJohn()

        val actor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(actor)

        val actorInfo: ActorDetailDto = env.dispatch(AuthAction.Actor_Get(actor.id))
        assertEquals(actor.id.value.toString(), actorInfo.id)
        assertEquals(env.johnUsername.value, actorInfo.subject)
        assertEquals(env.johnFullname.value, actorInfo.fullname)
        assertEquals(env.oidcService.oidcIssuer(), actorInfo.issuer)
    }

}