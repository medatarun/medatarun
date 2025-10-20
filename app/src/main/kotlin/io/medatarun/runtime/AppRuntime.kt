package io.medatarun.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.ModelQueries

interface AppRuntime {
    val extensionRegistry: ExtensionRegistry
    val modelCmd: ModelCmd
    val modelQueries: ModelQueries
}