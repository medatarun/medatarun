package io.medatarun.auth.infra.db

import io.medatarun.auth.fixtures.AuthEnvPlatformTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthDbMigrationStartupTest {
    /**
     * Auth must not need to call initSchema() manually anymore.
     *
     * Starting the platform with the DB extensions and AuthExtension must apply the contributed
     * SQL resources, create the tables, and record every applied step in the migration history.
     */
    @Test
    fun `auth startup applies contributed db migrations`() {
        val env = AuthEnvPlatformTest()
        assertTrue(env.dbMigrationChecker.tableExists("users"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_ctx"))
        assertTrue(env.dbMigrationChecker.tableExists("auth_code"))
        assertTrue(env.dbMigrationChecker.tableExists("actors"))
        assertEquals(1, env.dbMigrationChecker.migrationCount("platform-auth"))
        assertEquals(1, env.dbMigrationChecker.currentVersion("platform-auth"))

    }


}