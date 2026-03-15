package io.medatarun.model.infra.db

import io.medatarun.model.domain.fixtures.ModelTestEnv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelStorageDbMigrationStartupTest {
    @Test
    fun `models startup applies contributed db migrations`() {
        val env = ModelTestEnv()
        val dbMigrationChecker = env.dbMigrationChecker

        assertTrue(dbMigrationChecker.tableExists("model"))
        assertTrue(dbMigrationChecker.tableExists("model_event"))
        assertTrue(dbMigrationChecker.tableExists("model_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_type_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_entity_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_entity_attribute_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_relationship_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_relationship_role_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_relationship_attribute_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_search_item_snapshot"))
        assertTrue(dbMigrationChecker.tableExists("model_search_item_tag_snapshot"))
        assertEquals(1, dbMigrationChecker.migrationCount("models-core"))
        assertEquals(1, dbMigrationChecker.currentVersion("models-core"))
    }
}
