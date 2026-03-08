package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionCmdDescriptor
import io.medatarun.actions.domain.ActionSemantics

class ActionRegistered(
    val descriptor: ActionCmdDescriptor,
    val semantics: ActionSemantics
) {
}