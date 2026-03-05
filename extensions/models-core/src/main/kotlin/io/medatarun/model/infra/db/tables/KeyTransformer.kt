package io.medatarun.model.infra.db.tables

import io.medatarun.type.commons.key.Key
import org.jetbrains.exposed.v1.core.ColumnTransformer

class KeyTransformer<T : Key<T>>(val c: (value: String) -> T) : ColumnTransformer<String, T> {
    override fun unwrap(value: T): String {
        return value.asString()
    }

    override fun wrap(value: String): T {
        return Key.fromString(value, c)
    }

}