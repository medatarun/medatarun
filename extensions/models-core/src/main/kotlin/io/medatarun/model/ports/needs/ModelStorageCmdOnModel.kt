package io.medatarun.model.ports.needs

import io.medatarun.model.domain.ModelId
import kotlinx.serialization.Serializable

@Serializable
sealed interface ModelStorageCmdOnModel : ModelStorageCmd {
    val modelId: ModelId
}