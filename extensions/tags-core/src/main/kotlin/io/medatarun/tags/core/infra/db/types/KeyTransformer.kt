package io.medatarun.tags.core.infra.db.types

import io.medatarun.type.commons.key.Key
import org.jetbrains.exposed.v1.core.ColumnTransformer

class KeyTransformer<T : Key<T>>(private val constructor: (value: String) -> T) : ColumnTransformer<String, T> {
    override fun unwrap(value: T): String {
        return value.asString()
    }

    override fun wrap(value: String): T {
        return Key.fromString(value, constructor)
    }
}
