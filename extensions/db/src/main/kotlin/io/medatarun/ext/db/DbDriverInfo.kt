package io.medatarun.ext.db

import java.nio.file.Path

data class DbDriverInfo(
    val id: String,
    val name: String,
    val jarPath: Path,
    val className: String,
)