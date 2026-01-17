package io.medatarun.ext.db.internal.modelimport

data class IntrospectImportedKey(
    val pkTableCat: String?,
    val pkTableSchem: String?,
    val pkTableName: String,
    val pkColumnName: String,
    val fkTableCat: String?,
    val fkTableSchem: String?,
    val fkTableName: String,
    val fkColumnName: String,
    val keySeq: Short,
    val updateRule: IntrospectImportedKeyUpdateRule,
    val deleteRule: IntrospectImportedKeyDeleteRule,
    val fkName: String?,
    val pkName: String?,
    val deferrability: IntrospectImportedKeyDeferrability,
)