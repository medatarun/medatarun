package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class SecurityPermissionDto(val id: String, val name: String?, val description: String?, val implies: List<String>)