package io.medatarun.model.infra.db.tables

import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.util.UUID
import io.medatarun.type.commons.id.Id

class IdTransformer<T : Id<T>>(val c: (value: UUID) -> T) : ColumnTransformer<UUID, T> {
    override fun unwrap(value: T): UUID {
        return value.value
    }

    override fun wrap(value: UUID): T {
        return c(value)
    }

}
