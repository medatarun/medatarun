package io.medatarun.tags.core.infra.db

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.tags.core.TagTestEnv
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@EnableDatabaseTests
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
        assertTrue(dbMigrationChecker.tableExists("tag_view_current_tag_group"))
        assertTrue(dbMigrationChecker.tableExists("tag_view_current_tag"))
        assertTrue(dbMigrationChecker.tableExists("tag_view_history_tag_group"))
        assertTrue(dbMigrationChecker.tableExists("tag_view_history_tag"))
        assertTrue(dbMigrationChecker.tableExists("tag_event"))
        assertFalse(dbMigrationChecker.tableExists("tag_group"))
        assertFalse(dbMigrationChecker.tableExists("tag"))
        assertEquals(1, dbMigrationChecker.migrationCount("tags-core"))
        assertEquals(2, dbMigrationChecker.currentVersion("tags-core"))

    }

}
