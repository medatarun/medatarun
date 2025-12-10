package io.medatarun.model.internal

import io.medatarun.model.ports.exposed.ModelCmd

interface ModelAuditor {
    fun onCmdProcessed(cmd: ModelCmd)
}
