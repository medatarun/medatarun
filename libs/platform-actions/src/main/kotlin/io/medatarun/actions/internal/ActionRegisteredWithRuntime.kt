package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionDescriptor
import io.medatarun.actions.domain.ActionRegistered
import io.medatarun.actions.domain.ActionSemantics
import io.medatarun.actions.ports.needs.ActionProvider

internal data class ActionRegisteredWithRuntime(
    override val descriptor: ActionDescriptor,
    override val semantics: ActionSemantics,
    val provider: ActionProvider<*>
): ActionRegistered