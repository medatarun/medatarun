package io.medatarun.actions.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.model.ModelCmds
import io.medatarun.model.model.ModelHumanPrinter
import io.medatarun.model.model.ModelQueries

interface ActionCtx {
    val extensionRegistry: ExtensionRegistry
    val modelCmds: ModelCmds
    val modelQueries: ModelQueries
    val modelHumanPrinter: ModelHumanPrinter
    fun dispatchAction(req: ActionRequest):Any?
}