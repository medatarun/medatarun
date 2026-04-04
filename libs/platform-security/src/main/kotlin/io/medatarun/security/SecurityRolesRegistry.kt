package io.medatarun.security

interface SecurityRolesRegistry {
    fun findAllRoles(): List<AppPermission>
    fun findAllRenamedRoles(): Map<String, String>
}