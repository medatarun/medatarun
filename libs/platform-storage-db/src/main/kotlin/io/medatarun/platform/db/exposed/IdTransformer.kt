package io.medatarun.platform.db.exposed

import io.medatarun.type.commons.id.Id
import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.util.UUID

class IdTransformer<T : Id<T>>(private val constructor: (value: UUID) -> T) : ColumnTransformer<UUID, T> {
    override fun unwrap(value: T): UUID {
        return value.value
    }

    override fun wrap(value: UUID): T {
        return constructor(value)
    }
}