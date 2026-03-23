package io.medatarun.security

interface SecurityRolesProvider {
    fun getRoles(): List<AppPrincipalRole>

    /**
     * List of old keys to be renamed to new keys
     */
    fun getRenamedRoles(): Map<String, String> {
        return emptyMap()
    }
}