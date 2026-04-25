package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_Delete_Test {

    @Test
    fun `delete custom role`() {
        TODO()
    }

    @Test
    fun `delete role also removes it from role list`() {
        TODO()
    }

    @Test
    fun `refuse to delete a missing role`() {
        TODO()
    }

    @Test
    fun `refuse to delete a managed role`() {
        TODO()
    }

    @Test
    fun `refuse to delete role when user is not admin`() {
        TODO()
    }
}
