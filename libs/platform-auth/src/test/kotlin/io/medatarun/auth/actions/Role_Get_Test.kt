package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_Get_Test {

    @Test
    fun `can get custom role that do not have permissions`() {
        TODO()
    }

    @Test
    fun `can get custom role with its permissions`() {
        TODO()
    }

    @Test
    fun `can get managed role with managed marker and permissions`() {
        TODO()
    }

    @Test
    fun `refuse to get a missing role`() {
        TODO()
    }

    @Test
    fun `refuse to get role when user is not admin`() {
        TODO()
    }
}
