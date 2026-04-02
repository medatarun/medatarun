package io.medatarun.platform.db.exposed

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

/**
 * Registers a JSON column that is stored as `JSONB` on PostgreSQL and `TEXT` on SQLite.
 */
fun Table.jsonb(name: String): Column<String> = registerColumn(name, JsonbColumnType())
