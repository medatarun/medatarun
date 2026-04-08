package io.medatarun.auth.infra.db.tables

import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.platform.db.exposed.IdTransformer
import io.medatarun.platform.db.exposed.instant
import io.medatarun.platform.db.exposed.jsonb
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

internal object ActorTable : Table("auth_actor") {
    val id = javaUUID("id").transform(IdTransformer(::ActorId))
    val issuer = text("issuer")
    val subject = text("subject")
    val fullName = text("full_name")
    val email = text("email").nullable()
    val disabledDate = instant("disabled_date").nullable()
    val createdAt = instant("created_at")
    val lastSeenAt = instant("last_seen_at")
    override val primaryKey = PrimaryKey(id)
}
