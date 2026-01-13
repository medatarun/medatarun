package io.medatarun.security

interface SecurityRolesProvider {
    fun getRoles(): List<AppPrincipalRole>
}