package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_UpdateName_Test {

    @Test
    fun `change custom role name`() {
        TODO()
    }

    @Test
    fun `change managed role name`() {
        TODO()
    }

    @Test
    fun `refuse to change name of a missing role`() {
        TODO()
    }

    @Test
    fun `refuse to change role name when user is not admin`() {
        TODO()
    }
}
