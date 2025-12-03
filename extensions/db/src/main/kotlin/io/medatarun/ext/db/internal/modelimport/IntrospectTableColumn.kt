package io.medatarun.ext.db.internal.modelimport

data class IntrospectTableColumn(
    val columnName: String,
    val typeName: String,
    val remarks: String?,
    val columnSize: Int,
    val isNullableInt: Int,
    val decimalDigits: Int,
    val numPrecRadix: Int,
    val columnDef: String?,
    val charOctetLength: String?,
    val ordinalPosition: Int,
    val scopeCatalog: String?,
    val scopeSchema: String?,
    val scopeTable: String?,
    val isAutoIncrement: Boolean?,
    val isGeneratedColumn: Boolean?,
    val isNullable: Boolean?
)