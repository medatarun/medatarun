package io.medatarun.kernel

import java.nio.file.Path

interface MedatarunConfig {
    /**
     * Home of Medatarun runtime (where application is installed)
     */
    val applicationHomeDir: Path

    /**
     * Project directory, meaning the location where Medatarun is run, where user
     * stores its files
     */
    val projectDir: Path

    /**
     * Medatarun data directory, usually projectDir/.medatarun
     */
    val medatarunDir: Path
    fun getProperty(key: String): String?
    fun getProperty(key: String, defaultValue: String): String
}