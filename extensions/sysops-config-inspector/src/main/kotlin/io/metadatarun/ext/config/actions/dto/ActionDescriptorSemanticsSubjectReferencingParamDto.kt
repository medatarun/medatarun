package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActionDescriptorSemanticsSubjectReferencingParamDto(
    val name: String,
    val kind: String
)
