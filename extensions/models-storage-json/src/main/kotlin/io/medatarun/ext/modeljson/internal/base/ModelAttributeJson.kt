package io.medatarun.ext.modeljson.internal.base

import io.medatarun.ext.modeljson.internal.serializers.LocalizedTextMultiLangCompat
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class ModelAttributeJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedTextMultiLangCompat? = null,
    val description: @Contextual LocalizedTextMultiLangCompat? = null,
    val type: String,
    val optional: Boolean = false,
    val tags: List<String>? = emptyList()
)