package io.medatarun.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.domain.ModelCmds
import io.medatarun.model.domain.ModelHumanPrinter
import io.medatarun.model.domain.ModelQueries

interface AppRuntime {


    val extensionRegistry: ExtensionRegistry
    val modelCmds: ModelCmds
    val modelQueries: ModelQueries
    val modelHumanPrinter: ModelHumanPrinter


}