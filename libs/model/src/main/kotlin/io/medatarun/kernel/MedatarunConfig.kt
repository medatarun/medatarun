package io.medatarun.kernel

import java.nio.file.Path

interface MedatarunConfig {
     val projectDir: Path
    fun getProperty(key: String): String?
    fun getProperty(key: String, defaultValue: String): String
}