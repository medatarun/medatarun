package io.medatarun.ext.db.domain

import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
data class DbDriverInfo(
    val id: String,
    val name: String,
    val jarPath: Path,
    val className: String,
)