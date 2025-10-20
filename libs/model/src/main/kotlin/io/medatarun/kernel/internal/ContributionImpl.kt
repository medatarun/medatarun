package io.medatarun.kernel.internal

import io.medatarun.kernel.ContributionPointId
import io.medatarun.kernel.ExtensionId

data class ContributionImpl<CONTRIB: Any>(
    val fromExtensionId: ExtensionId,
    val toExtensionId: ExtensionId,
    val toContributinoPointId: ContributionPointId,
    val instance: CONTRIB
)