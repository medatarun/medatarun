package io.medatarun.actions.adapters

import io.medatarun.actions.domain.ActionInvoker
import io.medatarun.actions.domain.ActionRegistry
import io.medatarun.actions.internal.ActionRegistryImpl
import io.medatarun.platform.kernel.Service

/**
 * Action platform holds the public runnable tooling necessary to use actions.
 *
 * Exposed tools are the [ActionRegistryImpl] and [ActionInvoker], used to discover registered actions and invoke them.
 */

interface ActionPlatform: Service {
    val registry: ActionRegistry
    val invoker: ActionInvoker
}


