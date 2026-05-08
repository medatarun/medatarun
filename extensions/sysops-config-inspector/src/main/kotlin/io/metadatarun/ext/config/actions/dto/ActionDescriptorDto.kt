package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActionDescriptorDto(
    val actionRef: String,
    val title: String,
    val description: String?,
    val parameters: List<ActionParamDescriptorDto>,
    val securityRule: String,
    val semantics: ActionDescriptorSemanticsDto
) {
    fun actionGroupKey() = actionRef.split("/")[0]
    fun actionKey() = actionRef.split("/")[1]
}