package io.medatarun.security

import io.medatarun.platform.kernel.ExtensionRegistry

class SecurityRolesRegistryImpl(val extensionRegistry: ExtensionRegistry) : SecurityRolesRegistry {
    override fun findAllRoles(): List<AppPermission> {
        return extensionRegistry.findContributionsFlat(SecurityPermissionsProvider::class).flatMap { it.getPermissions() }
    }

    override fun findAllRenamedRoles(): Map<String, String> {
        val contribs = extensionRegistry.findContributionsFlat(SecurityPermissionsProvider::class)
        val all = mutableMapOf<String, String>()
        contribs.forEach { it.getRenamedPermissions().forEach { (k, v) -> all[k] = v } }
        return all
    }

}