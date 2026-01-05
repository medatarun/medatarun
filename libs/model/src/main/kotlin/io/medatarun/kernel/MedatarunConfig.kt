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

    fun getProperty(key: String): String?
    fun getProperty(key: String, defaultValue: String): String

    /**
     * Creates a new resource locator
     */
    fun createResourceLocator(): ResourceLocator
}