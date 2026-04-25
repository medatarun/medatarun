package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_UpdateKey_Test {

    @Test
    fun `change custom role key`() {
        TODO()
    }

    @Test
    fun `refuse to change custom role key to an existing role key`() {
        TODO()
    }

    @Test
    fun `refuse to change custom role key to a managed role key`() {
        TODO()
    }

    @Test
    fun `refuse to change key of a missing role`() {
        TODO()
    }

    @Test
    fun `refuse to change role key when user is not admin`() {
        TODO()
    }
}
