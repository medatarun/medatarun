package io.medatarun.ext.modeljson.internal

internal data class ModelsStorageJsonRepositoryConfig(
    val prettyPrint: Boolean
) {
    companion object {
        const val CONFIG_PRETTY_PRINT_KEY = "modeljson.prettyprint"
        const val CONFIG_PRETTY_PRINT_DEFAULT = "true"
    }
}