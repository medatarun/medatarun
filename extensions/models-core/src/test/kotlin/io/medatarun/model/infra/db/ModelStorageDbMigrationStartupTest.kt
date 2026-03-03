package io.medatarun.model.infra.db

import io.medatarun.model.domain.ModelTestEnv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelStorageDbMigrationStartupTest {
    @Test
    fun `models startup applies contributed db migrations`() {
        val env = ModelTestEnv()
        val dbMigrationChecker = env.dbMigrationChecker

        assertTrue(dbMigrationChecker.tableExists("model"))
        assertTrue(dbMigrationChecker.tableExists("model_type"))
        assertTrue(dbMigrationChecker.tableExists("entity"))
        assertTrue(dbMigrationChecker.tableExists("entity_attribute"))
        assertTrue(dbMigrationChecker.tableExists("relationship"))
        assertTrue(dbMigrationChecker.tableExists("relationship_role"))
        assertTrue(dbMigrationChecker.tableExists("relationship_attribute"))
        assertTrue(dbMigrationChecker.tableExists("denorm_model_search_item"))
        assertTrue(dbMigrationChecker.tableExists("denorm_model_search_item_tag"))
        assertEquals(1, dbMigrationChecker.migrationCount("models-core"))
        assertEquals(1, dbMigrationChecker.currentVersion("models-core"))
    }
}
