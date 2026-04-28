package io.medatarun.ext.modeljson.internal.v3

import kotlinx.serialization.Serializable

@Serializable
internal class ModelTypeJsonV3(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: String? = null,
    val description: String? = null,
)