package io.medatarun.ext.modeljson

import kotlinx.serialization.internal.NamedCompanion

data class ModelJsonRepositoryConfig(
    val prettyPrint: Boolean

) {
    companion object {
        const val CONFIG_PRETTY_PRINT_KEY="modeljson.prettyprint"
        const val CONFIG_PRETTY_PRINT_DEFAULT="true"
        const val CONFIG_REPOSITORY_PATH_KEY="modeljson.repository.path"
    }
}