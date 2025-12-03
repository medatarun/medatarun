package io.medatarun.ext.db.model

import java.nio.file.Path

data class DbDriverInfo(
    val id: String,
    val name: String,
    val jarPath: Path,
    val className: String,
)