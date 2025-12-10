package io.medatarun.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelHumanPrinter
import io.medatarun.model.ports.exposed.ModelQueries

interface AppRuntime {


    val extensionRegistry: ExtensionRegistry
    val modelCmds: ModelCmds
    val modelQueries: ModelQueries
    val modelHumanPrinter: ModelHumanPrinter


}