package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.ContributionPointId
import io.medatarun.platform.kernel.ExtensionId
import kotlin.reflect.KClass


data class ContributionPoint<CONTRIB : Any>(
    val id: ContributionPointId,
    val api: KClass<CONTRIB>,
    val extensionId: ExtensionId
)