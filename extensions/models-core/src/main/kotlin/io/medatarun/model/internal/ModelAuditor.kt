package io.medatarun.model.internal

import io.medatarun.model.ports.exposed.ModelCmdEnveloppe

interface ModelAuditor {
    fun onCmdProcessed(cmd: ModelCmdEnveloppe)
}
