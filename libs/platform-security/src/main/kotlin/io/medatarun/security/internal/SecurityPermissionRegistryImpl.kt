package io.medatarun.security.internal

import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionKey
import io.medatarun.security.SecurityPermissionRegistry
import io.medatarun.security.SecurityPermissionsProvider
import kotlin.getValue

internal class SecurityPermissionRegistryImpl(val extensionRegistry: ExtensionRegistry) : SecurityPermissionRegistry {

    val list: Map<AppPermissionKey, AppPermission> by lazy {
        extensionRegistry.findContributionsFlat(SecurityPermissionsProvider::class)
            .flatMap { it.getPermissions() }
            .associateBy { it.key }
    }

    override fun findAll(): List<AppPermission> {
        return list.values.toList()
    }

    override fun findByKeyOptional(key: AppPermissionKey): AppPermission? {
        return list[key]
    }

    override fun findAllRenamed(): Map<String, AppPermissionKey> {
        val contribs = extensionRegistry.findContributionsFlat(SecurityPermissionsProvider::class)
        val all = mutableMapOf<String, AppPermissionKey>()
        contribs.forEach { it.getRenamedPermissions().forEach { (k, v) -> all[k] = v } }
        return all
    }


}