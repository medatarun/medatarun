package io.medatarun.model.internal

import io.medatarun.model.domain.ModelCmd

interface ModelAuditor {
    fun onCmdProcessed(cmd: ModelCmd)
}
