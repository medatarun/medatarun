package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.ContributionPointId
import io.medatarun.platform.kernel.ExtensionId

data class ContributionImpl<CONTRIB: Any>(
    val fromExtensionId: ExtensionId,
    val toExtensionId: ExtensionId,
    val toContributinoPointId: ContributionPointId,
    val instance: CONTRIB
)