package io.medatarun.ext.db

import io.medatarun.model.model.AttributeDefId

data class IntrospectTable(
    val tableCat: String?,
    val tableSchem: String?,
    val tableName: String,
    val tableType: String,
    val remarks: String?,
    val typeCat: String?,
    val typeSchem: String?,
    val typeName: String?,
    val selfReferencingColName: String?,
    val refGeneration: String?,
    val columns: List<IntrospectTableColumn>,
    val primaryKey: List<IntrospectPk>
) {
    fun pkNameOrFirstColumn(): AttributeDefId {
        val id = primaryKey.firstOrNull()?.let { pkPart ->
            columns.firstOrNull { column -> column.columnName == pkPart.columnName }?.columnName
        } ?: columns.firstOrNull()?.columnName
        ?: throw DbTableWithoutColumnsException(tableName)

        return AttributeDefId(id)
    }
}