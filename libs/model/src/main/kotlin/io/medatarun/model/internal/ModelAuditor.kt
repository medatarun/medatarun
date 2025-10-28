package io.medatarun.model.internal

import io.medatarun.model.model.ModelCmd

interface ModelAuditor {
    fun onCmdProcessed(cmd: ModelCmd)
}
