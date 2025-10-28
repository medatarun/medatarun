package io.medatarun.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.model.ModelCmds
import io.medatarun.model.model.ModelQueries

interface AppRuntime {
    val extensionRegistry: ExtensionRegistry
    val modelCmds: ModelCmds
    val modelQueries: ModelQueries
}