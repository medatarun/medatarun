package io.medatarun.auth.actions

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class User_ChangeFullname_Test {
    @Test
    fun `change user full name`() {
        val env = AuthEnvTest()
        val fullnameNext = Fullname("New John Doe")
        env.createJohn()
        env.asAdmin()
        @Suppress("UnusedVariable", "unused")
        val result: Unit = env.dispatch(AuthAction.User_ChangeFullname(env.johnUsername, fullnameNext))

        env.logout()

        // Test on our user database
        val user = env.userService.loginUser(env.johnUsername, env.johnPassword)
        assertEquals(fullnameNext, user.fullname)

        env.asUser(env.johnUsername)

        // We can test with whoami, which makes sure that it propagated to actors
        val whoami = env.dispatch(AuthAction.WhoAmI())
        assertEquals(fullnameNext.value, whoami.fullname)

        // Make sure there are no side effects on other users (bad update directive or something like that)
        val actoradmin = assertNotNull(env.actorService.findByIssuerAndSubjectOptional(env.oidcService.oidcIssuer(), env.adminUsername.value))
        assertEquals(env.adminFullname.value, actoradmin.fullname)

        val userAdmin = assertNotNull(env.userService.findByUsername(env.adminUsername))
        assertEquals(env.adminFullname, userAdmin.fullname)

    }

}