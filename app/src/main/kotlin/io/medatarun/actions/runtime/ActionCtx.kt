package io.medatarun.actions.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.domain.ModelCmds
import io.medatarun.model.domain.ModelHumanPrinter
import io.medatarun.model.domain.ModelQueries

interface ActionCtx {
    val extensionRegistry: ExtensionRegistry
    val modelCmds: ModelCmds
    val modelQueries: ModelQueries
    val modelHumanPrinter: ModelHumanPrinter
    fun dispatchAction(req: ActionRequest):Any?
}