package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActionDescriptorSemanticsDto(
    val intent: String,
    val subjects: List<ActionDescriptorSemanticsSubjectDto>,
    val returns: List<String>
)
