package io.medatarun.actions.infra.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionAuditRecorderDbMigrationStartupTest {
    @Test
    fun `platform actions storage startup applies contributed db migrations`() {
        val env = ActionAuditDbTestEnv()

        assertTrue(env.dbMigrationChecker.tableExists("action_audit_event"))
        assertEquals(1, env.dbMigrationChecker.migrationCount("platform-actions-storage-db"))
        assertEquals(1, env.dbMigrationChecker.currentVersion("platform-actions-storage-db"))
    }
}
