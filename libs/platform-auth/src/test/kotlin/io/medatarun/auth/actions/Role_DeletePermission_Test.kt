package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_DeletePermission_Test {

    @Test
    fun `delete existing permission from a custom role`() {
        TODO()
    }

    @Test
    fun `delete one permission without deleting the other role permissions`() {
        TODO()
    }

    @Test
    fun `refuse to delete a permission that the role does not have`() {
        TODO()
    }

    @Test
    fun `refuse to change permissions of a managed role`() {
        TODO()
    }
}
