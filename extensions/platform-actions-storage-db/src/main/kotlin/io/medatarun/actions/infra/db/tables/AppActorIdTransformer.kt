package io.medatarun.actions.infra.db.tables

import io.medatarun.security.AppActorId
import io.medatarun.type.commons.id.Id
import org.jetbrains.exposed.v1.core.ColumnTransformer

class AppActorIdTransformer : ColumnTransformer<String, AppActorId> {
    override fun unwrap(value: AppActorId): String {
        return value.asString()
    }

    override fun wrap(value: String): AppActorId {
        return Id.fromString(value, ::AppActorId)
    }
}
