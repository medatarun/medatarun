package io.medatarun.actions.runtime

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelHumanPrinter
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.runtime.AppRuntime

class ActionCtxFactory(val runtime: AppRuntime, val actionRegistry: ActionRegistry) {
    fun create(): ActionCtx {
        return object: ActionCtx {
            override val extensionRegistry: ExtensionRegistry = runtime.extensionRegistry
            override val modelCmds: ModelCmds = runtime.modelCmds
            override val modelQueries: ModelQueries = runtime.modelQueries
            override val modelHumanPrinter: ModelHumanPrinter = runtime.modelHumanPrinter
            override fun dispatchAction(req: ActionRequest): Any? {
                return actionRegistry.handleInvocation(req, this)
            }
        }
    }
}