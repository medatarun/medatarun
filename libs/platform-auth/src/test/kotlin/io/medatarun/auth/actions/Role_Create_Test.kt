package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_Create_Test {

    @Test
    fun `create custom role with name key and description`() {
        TODO()
    }

    @Test
    fun `create custom role without description`() {
        TODO()
    }

    @Test
    fun `refuse to create two roles with the same key`() {
        TODO()
    }

    @Test
    fun `refuse to create a role with a managed role key`() {
        TODO()
    }
}
