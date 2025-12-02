package io.medatarun.ext.db

data class IntrospectPk(
    val tableName: String,
    val columnName: String,
    val keySeq: Short,
    val pkName: String?
)