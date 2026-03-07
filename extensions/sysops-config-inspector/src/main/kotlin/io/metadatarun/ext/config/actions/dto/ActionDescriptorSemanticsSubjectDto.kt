package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActionDescriptorSemanticsSubjectDto(
    val type: String,
    val referencingParams: List<String>
)