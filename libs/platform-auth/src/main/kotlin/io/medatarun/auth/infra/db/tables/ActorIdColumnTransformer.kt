package io.medatarun.auth.infra.db.tables

import io.medatarun.auth.domain.actor.ActorId
import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.util.UUID

internal class ActorIdColumnTransformer : ColumnTransformer<UUID, ActorId> {
    override fun unwrap(value: ActorId): UUID {
        return value.value
    }

    override fun wrap(value: UUID): ActorId {
        return ActorId(value)
    }
}