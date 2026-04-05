package io.medatarun.auth.infra.db.tables

import io.medatarun.auth.infra.db.types.ActorPermissionTransformer
import org.jetbrains.exposed.v1.core.Table

internal object RolePermissionTable : Table("auth_role_permission") {
    val authRoleId = reference("auth_role_id", RoleTable.id)
    val permission = varchar("permission", 50).transform(ActorPermissionTransformer())
    override val primaryKey = PrimaryKey(authRoleId, permission)
}
