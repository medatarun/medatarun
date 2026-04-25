package io.medatarun.auth.internal.actors

import io.medatarun.auth.domain.role.Role
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleInMemory
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.lang.uuid.UuidUtils
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset

/**
 * Managed roles are roles managed by the application.
 *
 * It means you cannot change its permissions, name, description and will be
 * recreated at every startup if missing in storage.
 */
class ManagedRoles {

    /**
     * Returns true if one is those roles is a managed role.
     */
    fun isManagedRole(key: RoleKey): Boolean {
        return managedRoles.any { it.key == key }
    }

    companion object {
        val ADMIN_ROLE_KEY = RoleKey("admin")
        val adminRole: Role = RoleInMemory(
            id = RoleId(UuidUtils.generateV7()),
            key = ADMIN_ROLE_KEY,
            name = "Admin",
            description = null,
            createdAt = LocalDate.of(2025, Month.JANUARY, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
            lastUpdatedAt = LocalDate.of(2025, Month.JANUARY, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
        )
        val MANAGER_ROLE_KEY = RoleKey("manager")
        val managerRole: Role = RoleInMemory(
            id = RoleId(UuidUtils.generateV7()),
            key = MANAGER_ROLE_KEY,
            name = "Manager",
            description = "Can read and write models, manage global tags.",
            createdAt = LocalDate.of(2025, Month.JANUARY, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
            lastUpdatedAt = LocalDate.of(2025, Month.JANUARY, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
        )
        val READER_ROLE_KEY = RoleKey("reader")
        val readerRole: Role = RoleInMemory(
            id = RoleId(UuidUtils.generateV7()),
            key = READER_ROLE_KEY,
            name = "Reader",
            description = "Can read models and tags but not change anything.",
            createdAt = LocalDate.of(2025, Month.JANUARY, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
            lastUpdatedAt = LocalDate.of(2025, Month.JANUARY, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
        )
        val managedRoles = listOf(adminRole, managerRole, readerRole)
    }
}