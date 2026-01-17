package io.medatarun.model.ports.exposed

/**
 * Commands to create, delete and changes modes, entity definitions, entity definition's attributes definitions
 * and relationships.
 *
 * This is a command based interface, meaning that all you can do is described by commands (see [ModelCmd] for the
 * list of possible actions)
 *
 */
interface ModelCmds {

    /**
     * Execute this command
     */
    fun dispatch(cmd: ModelCmd)
}