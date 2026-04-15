package io.medatarun.ext.modeljson.internal.base

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class ModelAttributeJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val type: String,
    val optional: Boolean = false,
    val tags: List<String>? = emptyList()
)