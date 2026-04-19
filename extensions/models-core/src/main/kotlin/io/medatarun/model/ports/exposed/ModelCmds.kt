package io.medatarun.model.ports.exposed

import io.medatarun.platform.kernel.Service

/**
 * Commands to create, delete and changes modes, entity definitions, entity definition's attributes definitions
 * and relationships.
 *
 * This is a command based interface, meaning that all you can do is described by commands (see [ModelCmd] for the
 * list of possible actions)
 *
 */
interface ModelCmds: Service {

    /**
     * Execute this command
     */
    fun dispatch(cmdEnv: ModelCmdEnveloppe)

    /**
     * Rebuilds model projections by replaying persisted events.
     */
    fun maintenanceRebuildCaches()
}
