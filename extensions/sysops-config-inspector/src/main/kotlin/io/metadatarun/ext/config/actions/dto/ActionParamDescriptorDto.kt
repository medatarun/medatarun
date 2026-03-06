package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActionParamDescriptorDto(
    val name: String,
    val type: String,
    val jsonType: String,
    val optional: Boolean,
    val title: String?,
    val description: String?,
    val order: Int
)