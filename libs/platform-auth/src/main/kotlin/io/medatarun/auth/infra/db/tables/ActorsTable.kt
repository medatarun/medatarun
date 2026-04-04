package io.medatarun.auth.infra.db.tables

import io.medatarun.platform.db.exposed.jsonb
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

internal object ActorsTable : Table("actors") {
    val idColumn = javaUUID("id").transform(ActorIdColumnTransformer())
    val issuerColumn = text("issuer")
    val subjectColumn = text("subject")
    val fullNameColumn = text("full_name")
    val emailColumn = text("email").nullable()
    val rolesJsonColumn = jsonb("roles_json")
    val disabledDateColumn = timestamp("disabled_date").nullable()
    val createdAtColumn = timestamp("created_at")
    val lastSeenAtColumn = timestamp("last_seen_at")
}

