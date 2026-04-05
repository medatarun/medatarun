package io.medatarun.auth.infra.db.tables

import org.jetbrains.exposed.v1.core.Table

internal object ActorRoleTable : Table("auth_actor_role") {
    val actorId = reference("auth_actor_id", ActorTable.id)
    val roleId = reference("auth_role_id", RoleTable.id)
    override val primaryKey = PrimaryKey(actorId, roleId)
}
