package io.medatarun.security

interface SecurityRolesRegistry {
    fun findAllRoles(): List<AppPrincipalRole>
}