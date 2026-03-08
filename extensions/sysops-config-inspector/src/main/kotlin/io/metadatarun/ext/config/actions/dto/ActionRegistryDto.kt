package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActionRegistryDto(
    val items: List<ActionDescriptorDto>
)