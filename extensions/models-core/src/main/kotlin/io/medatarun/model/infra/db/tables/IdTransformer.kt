package io.medatarun.model.infra.db.tables

import io.medatarun.type.commons.id.Id
import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.util.UUID

class IdTransformer<T : Id<T>>(val c: (value: UUID) -> T) : ColumnTransformer<String, T> {
    override fun unwrap(value: T): String {
        return value.asString()
    }

    override fun wrap(value: String): T {
        return Id.fromString(value, c)
    }

}