package io.medatarun.security

import io.medatarun.platform.kernel.Service

/**
 * Contains all descriptions of all permissions
 */
interface SecurityPermissionRegistry : Service {
    /**
     * Lists all permission descriptions
     */
    fun findAll(): List<AppPermission>

    /**
     * Lists all renamed permissions
     */
    fun findAllRenamed(): Map<String, AppPermissionKey>

    /**
     * Find a permission using its key, returns null if not found
     */
    fun findByKeyOptional(key: AppPermissionKey): AppPermission?
}