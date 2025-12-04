package io.medatarun.resources

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.model.ModelCmds
import io.medatarun.model.model.ModelHumanPrinter
import io.medatarun.model.model.ModelQueries
import io.medatarun.runtime.AppRuntime

class ActionCtxFactory(val runtime: AppRuntime, val resourceRepository: ResourceRepository) {
    fun create(): ActionCtx {
        return object:ActionCtx {
            override val extensionRegistry: ExtensionRegistry = runtime.extensionRegistry
            override val modelCmds: ModelCmds = runtime.modelCmds
            override val modelQueries: ModelQueries = runtime.modelQueries
            override val modelHumanPrinter: ModelHumanPrinter = runtime.modelHumanPrinter
            override fun dispatchAction(req: ResourceInvocationRequest): Any? {
                return resourceRepository.handleInvocation(req, this)
            }
        }
    }
}