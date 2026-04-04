package io.medatarun.auth.infra.db.tables

import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.platform.db.exposed.IdTransformer
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

internal object ActorRoleTable : Table("auth_actor_role") {
    val actorId = javaUUID("auth_actor_id").transform(IdTransformer(::ActorId))
    val roleId = javaUUID("auth_role_id").transform(IdTransformer(::RoleId))
}