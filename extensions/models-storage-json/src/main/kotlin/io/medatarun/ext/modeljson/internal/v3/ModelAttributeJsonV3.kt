package io.medatarun.ext.modeljson.internal.v3

import kotlinx.serialization.Serializable

@Serializable
internal class ModelAttributeJsonV3(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: String? = null,
    val description: String? = null,
    val type: String,
    val optional: Boolean = false,
    val tags: List<String>? = emptyList()
)