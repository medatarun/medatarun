package io.medatarun.tags.core.domain

import io.medatarun.platform.kernel.Service

/**
 * Executes tag commands through an explicit envelope.
 *
 * The envelope carries the originating action and actor identifiers so tag command handlers
 * can keep the call identity available without depending on the action layer directly.
 */
interface TagCmds: Service {
    fun dispatch(cmdEnv: TagCmdEnveloppe)
    fun maintenanceRebuildCaches()
}
