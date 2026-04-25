package io.medatarun.auth.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test

@EnableDatabaseTests
class Role_UpdateDescription_Test {

    @Test
    fun `change custom role description`() {
        TODO()
    }

    @Test
    fun `remove custom role description`() {
        TODO()
    }

    @Test
    fun `change managed role description`() {
        TODO()
    }

    @Test
    fun `refuse to change description of a missing role`() {
        TODO()
    }

    @Test
    fun `refuse to change role description when user is not admin`() {
        TODO()
    }
}
