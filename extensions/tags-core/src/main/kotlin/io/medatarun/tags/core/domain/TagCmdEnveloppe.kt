package io.medatarun.tags.core.domain

import io.medatarun.security.AppTraceabilityRecord

/**
 * Command envelope used by tag commands to keep the originating action and actor.
 *
 * The tag command layer does not depend on the action system directly; it only consumes
 * this envelope when it needs to carry call identity further down the stack.
 */
data class TagCmdEnveloppe(
    val traceabilityRecord: AppTraceabilityRecord,
    val cmd: TagCmd,
)
