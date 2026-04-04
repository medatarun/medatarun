package io.medatarun.auth.infra.db.tables

import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.infra.db.types.AppPermissionTransformer
import io.medatarun.platform.db.exposed.IdTransformer
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

internal object RolePermissionTable : Table("auth_role_permission") {
    val authRoleId = javaUUID("auth_role_id").transform(IdTransformer(::RoleId))
    val permission = varchar("permission", 50).transform(AppPermissionTransformer())
}
