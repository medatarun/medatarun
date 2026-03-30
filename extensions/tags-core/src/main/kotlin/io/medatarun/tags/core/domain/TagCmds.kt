package io.medatarun.tags.core.domain

/**
 * Executes tag commands through an explicit envelope.
 *
 * The envelope carries the originating action and actor identifiers so tag command handlers
 * can keep the call identity available without depending on the action layer directly.
 */
interface TagCmds {
    fun dispatch(cmdEnv: TagCmdEnveloppe)
    fun maintenanceRebuildCaches()
}
