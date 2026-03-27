package io.medatarun.tags.core.infra.db

import io.medatarun.tags.core.TagTestEnv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TagsCoreDbMigrationStartupTest {
    /**
     * Tags schema now comes from a migration contribution instead of a startup callback in the extension.
     *
     * This test starts the real platform and checks that the tag tables and the migration history entry
     * are created before the services are used.
     */
    @Test
    fun `tags startup applies contributed db migrations`() {
        val env = TagTestEnv()
        val dbMigrationChecker = env.dbMigrationChecker
        assertTrue(dbMigrationChecker.tableExists("tag_group_projection"))
        assertTrue(dbMigrationChecker.tableExists("tag_projection"))
        assertTrue(dbMigrationChecker.tableExists("tag_group_history_projection"))
        assertTrue(dbMigrationChecker.tableExists("tag_history_projection"))
        assertTrue(dbMigrationChecker.tableExists("tag_event"))
        assertEquals(1, dbMigrationChecker.migrationCount("tags-core"))
        assertEquals(2, dbMigrationChecker.currentVersion("tags-core"))

    }

}
