package io.medatarun.auth.infra.db

import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppActorSystemMaintenance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class AuthDbMigrationStartupTest {
    /**
     * Auth must not need to call initSchema() manually anymore.
     *
     * Starting the platform with the DB extensions and AuthExtension must apply the contributed
     * SQL resources, create the tables, and record every applied step in the migration history.
     */
    @Test
    fun `auth startup applies contributed db migrations`() {
        val env = AuthEnvTest()

        // Checks various tables exist
        assertTrue(env.dbMigrationChecker.tableExists("users"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_ctx"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_code"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_actor"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_role"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_role_permission"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_actor_role"))

        // We are always doing a fresh install. Even if the currentVersion
        // changes, the count of lines of migration is always 1, because it is
        // a fresh install and not a migration.
        assertEquals(1, env.dbMigrationChecker.migrationCount("platform-auth"))
        // Checks that it is the right version number
        assertEquals(2, env.dbMigrationChecker.currentVersion("platform-auth"))

        // Cheks that actor for system maintenance exists in ActorStorage
        val actorId = ActorId(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID)
        val systemMaintenanceActor = env.actorService.findByIdOptional(actorId)
        assertNotNull(systemMaintenanceActor)
        assertEquals(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ISSUER, systemMaintenanceActor.issuer)
        assertEquals(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_SUBJECT, systemMaintenanceActor.subject)
        assertEquals(AppActorSystemMaintenance.displayName, systemMaintenanceActor.fullname)
    }
}
