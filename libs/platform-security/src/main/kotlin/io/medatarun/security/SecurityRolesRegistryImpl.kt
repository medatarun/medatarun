package io.medatarun.security

import io.medatarun.platform.kernel.ExtensionRegistry

class SecurityRolesRegistryImpl(val extensionRegistry: ExtensionRegistry) : SecurityRolesRegistry {
    override fun findAllRoles(): List<AppPrincipalRole> {
        return extensionRegistry.findContributionsFlat(SecurityRolesProvider::class).flatMap { it.getRoles() }
    }

    override fun findAllRenamedRoles(): Map<String, String> {
        val contribs = extensionRegistry.findContributionsFlat(SecurityRolesProvider::class)
        val all = mutableMapOf<String, String>()
        contribs.forEach { it.getRenamedRoles().forEach { (k, v) -> all[k] = v } }
        return all
    }

}