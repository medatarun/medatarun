package io.medatarun.security

interface SecurityRolesRegistry {
    fun findAllRoles(): List<AppPrincipalRole>
    fun findAllRenamedRoles(): Map<String, String>
}