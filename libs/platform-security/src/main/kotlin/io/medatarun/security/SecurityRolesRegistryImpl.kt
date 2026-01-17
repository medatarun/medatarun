package io.medatarun.security

import io.medatarun.platform.kernel.ExtensionRegistry

class SecurityRolesRegistryImpl(val extensionRegistry: ExtensionRegistry): SecurityRolesRegistry {
    override fun findAllRoles(): List<AppPrincipalRole> {
        return extensionRegistry.findContributionsFlat(SecurityRolesProvider::class).flatMap { it.getRoles() }
    }


}