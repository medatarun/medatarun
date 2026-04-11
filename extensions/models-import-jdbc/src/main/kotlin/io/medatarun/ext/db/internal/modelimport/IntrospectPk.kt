package io.medatarun.ext.db.internal.modelimport

internal data class IntrospectPk(
    val tableName: String,
    val columnName: String,
    val keySeq: Short,
    val pkName: String?
)