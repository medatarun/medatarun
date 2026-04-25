package io.medatarun.security

import io.medatarun.platform.kernel.ServiceContributionPoint

/**
 * Interface that permission providers must implement to declare
 * their permissions in the application.
 */
interface SecurityPermissionsProvider: ServiceContributionPoint {

    /**
     * List of permissions provided by this provider
     */
    fun getPermissions(): List<AppPermission>

    /**
     * List of old keys to be renamed to new keys
     *
     * The key of the map is the key of the old permission to remove
     *
     * The value of the map is the key of the new permission
     */
    fun getRenamedPermissions(): Map<String, AppPermissionKey> {
        return emptyMap()
    }
}