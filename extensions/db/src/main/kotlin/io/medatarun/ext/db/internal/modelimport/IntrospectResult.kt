package io.medatarun.ext.db.internal.modelimport

class IntrospectResult(
    val tables: List<IntrospectTable>,

    ) {
    fun types(): Set<String> {
        return tables.map { t -> t.columns.map { c -> c.typeName } }
            .flatten()
            .toSet()
    }

    fun isNullableOrUndefined(tableName: String, columnName: String): Boolean {
        val table = tables.first { it.tableName == tableName}
        val column = table.columns.first { it.columnName == columnName }
        return column.isNullable ?: true
    }
}