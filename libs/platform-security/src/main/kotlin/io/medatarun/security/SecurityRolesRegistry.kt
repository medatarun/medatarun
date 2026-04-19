package io.medatarun.security

import io.medatarun.platform.kernel.Service


interface SecurityRolesRegistry: Service {
    fun findAllRoles(): List<AppPermission>
    fun findAllRenamedRoles(): Map<String, String>
}