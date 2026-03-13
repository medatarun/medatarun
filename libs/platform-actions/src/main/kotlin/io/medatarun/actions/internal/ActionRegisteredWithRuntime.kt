package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionCmdDescriptor
import io.medatarun.actions.domain.ActionSemantics
import io.medatarun.actions.ports.needs.ActionProvider

data class ActionRegisteredWithRuntime(
    override val descriptor: ActionCmdDescriptor,
    override val semantics: ActionSemantics,
    val provider: ActionProvider<*>
): ActionRegistered