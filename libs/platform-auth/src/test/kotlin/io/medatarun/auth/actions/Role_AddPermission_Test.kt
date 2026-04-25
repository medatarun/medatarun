package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_AddPermission_Test {

    @Test
    fun `add known permission to a custom role`() {
        TODO()
    }

    @Test
    fun `add several known permissions to the same custom role`() {
        TODO()
    }

    @Test
    fun `refuse to add the same permission twice to a role`() {
        TODO()
    }

    @Test
    fun `refuse to add an unknown permission to a role`() {
        TODO()
    }

    @Test
    fun `refuse to change permissions of a managed role`() {
        TODO()
    }
}
