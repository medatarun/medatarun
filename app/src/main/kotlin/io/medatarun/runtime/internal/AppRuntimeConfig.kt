package io.medatarun.runtime.internal

import java.nio.file.Path

class AppRuntimeConfig(
    val projectDir: Path,
    val modelJsonRepositoryPath: Path
)