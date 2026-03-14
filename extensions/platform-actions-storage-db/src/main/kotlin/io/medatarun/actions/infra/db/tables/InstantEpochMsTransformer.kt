package io.medatarun.actions.infra.db.tables

import org.jetbrains.exposed.v1.core.ColumnTransformer
import java.time.Instant

class InstantEpochMsTransformer : ColumnTransformer<Long, Instant> {
    override fun unwrap(value: Instant): Long {
        return value.toEpochMilli()
    }

    override fun wrap(value: Long): Instant {
        return Instant.ofEpochMilli(value)
    }
}
