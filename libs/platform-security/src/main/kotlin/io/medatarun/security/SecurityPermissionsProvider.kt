package io.medatarun.security

/**
 * Interface that permission providers must implement to declare
 * their permissions in the application.
 */
interface SecurityPermissionsProvider {

    /**
     * List of permissions provided by this provider
     */
    fun getPermissions(): List<AppPermission>

    /**
     * List of old keys to be renamed to new keys
     */
    fun getRenamedPermissions(): Map<String, String> {
        return emptyMap()
    }
}