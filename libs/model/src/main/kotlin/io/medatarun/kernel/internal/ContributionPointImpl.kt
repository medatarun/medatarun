package io.medatarun.kernel.internal

import io.medatarun.kernel.ContributionPointId
import io.medatarun.kernel.ExtensionId
import kotlin.reflect.KClass


internal data class ContributionPoint<CONTRIB : Any>(
    val id: ContributionPointId,
    val api: KClass<CONTRIB>,
    val extensionId: ExtensionId
)