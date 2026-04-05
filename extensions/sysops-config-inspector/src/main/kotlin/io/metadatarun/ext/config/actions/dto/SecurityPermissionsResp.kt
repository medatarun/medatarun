package io.metadatarun.ext.config.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class SecurityPermissionsResp(val items: List<SecurityPermissionDto>)