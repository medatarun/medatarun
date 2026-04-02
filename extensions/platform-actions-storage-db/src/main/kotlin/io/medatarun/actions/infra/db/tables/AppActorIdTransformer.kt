package io.medatarun.actions.infra.db.tables

import io.medatarun.security.AppActorId
import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.util.UUID

class AppActorIdTransformer : ColumnTransformer<UUID, AppActorId> {
    override fun unwrap(value: AppActorId): UUID {
        return value.value
    }

    override fun wrap(value: UUID): AppActorId {
        return AppActorId(value)
    }
}
