package io.medatarun.auth.actions

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.internal.actors.ManagedRoles
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppPermissionCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class RoleList_Test {

    @Test
    fun `all managed roles are created at startup`() {
        val readPermission = "test.read"
        val writePermission = "test.write"
        val adminPermission = "test.admin"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(readPermission, AppPermissionCategory.READ),
                AuthEnvTest.TestOtherPermission(writePermission, AppPermissionCategory.WRITE),
                AuthEnvTest.TestOtherPermission(adminPermission, AppPermissionCategory.ADMIN_SCOPE),
            )
        )
        env.asAdmin()

        val foundRoles = env.dispatch(AuthAction.RoleList()).items
        val foundRoleKeys = foundRoles.associateBy { it.key }
        val expectedManagedRoleKeys = setOf(
            ManagedRoles.ADMIN_ROLE_KEY.value,
            ManagedRoles.READER_ROLE_KEY.value,
            ManagedRoles.MANAGER_ROLE_KEY.value
        )
        assertEquals(expectedManagedRoleKeys, foundRoleKeys.keys)

        val adminRole = foundRoleKeys[ManagedRoles.ADMIN_ROLE_KEY.value]
        assertNotNull(adminRole)
        assertTrue(adminRole.managedRole)

        val readerRole = foundRoleKeys[ManagedRoles.READER_ROLE_KEY.value]
        assertNotNull(readerRole)
        assertTrue(readerRole.managedRole)

        val managerRole = foundRoleKeys[ManagedRoles.MANAGER_ROLE_KEY.value]
        assertNotNull(managerRole)
        assertTrue(managerRole.managedRole)

        val adminDetails = env.dispatch(AuthAction.RoleGet(RoleRef.ByKey(ManagedRoles.ADMIN_ROLE_KEY)))
        assertTrue(adminDetails.permissions.toSet().containsAll(setOf(ActorPermission.ADMIN.key, adminPermission)))

        val readerDetails = env.dispatch(AuthAction.RoleGet(RoleRef.ByKey(ManagedRoles.READER_ROLE_KEY)))
        assertEquals(setOf(readPermission), readerDetails.permissions.toSet())

        val managerDetails = env.dispatch(AuthAction.RoleGet(RoleRef.ByKey(ManagedRoles.MANAGER_ROLE_KEY)))
        assertEquals(setOf(readPermission, writePermission), managerDetails.permissions.toSet())
    }

    @Test
    fun `create role then find it in list`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleKey = RoleKey("test-role")

        env.dispatch(
            AuthAction.RoleCreate(
                key = roleKey,
                name = "Test role",
                description = "Role created by the action test"
            )
        )

        val foundRoles = env.dispatch(AuthAction.RoleList()).items
        assertEquals(4, foundRoles.size) // 3 built-in roles and the one we created
        val createdRole = foundRoles.firstOrNull { it.key == roleKey.value }

        assertNotNull(createdRole)
        assertEquals("Test role", createdRole.name)
        assertEquals("Role created by the action test", createdRole.description)
        assertEquals(false, createdRole.managedRole)
    }
}
