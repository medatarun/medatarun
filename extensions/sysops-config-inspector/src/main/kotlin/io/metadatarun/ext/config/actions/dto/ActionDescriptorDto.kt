package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActionDescriptorDto(
    val id: String,
    val groupKey: String,
    val actionKey: String,
    val title: String,
    val description: String?,
    val parameters: List<ActionParamDescriptorDto>,
    val securityRule: String,
    val semantics: ActionDescriptorSemanticsDto
)