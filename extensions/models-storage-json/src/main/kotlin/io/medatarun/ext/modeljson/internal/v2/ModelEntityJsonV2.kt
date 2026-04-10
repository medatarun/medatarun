package io.medatarun.ext.modeljson.internal.v2

import io.medatarun.ext.modeljson.internal.base.ModelAttributeJson
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class ModelEntityJsonV2(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val identifierAttribute: @Contextual String,
    val origin: String? = null,
    val tags: List<String>? = emptyList(),
    val attributes: List<ModelAttributeJson>,
    val documentationHome: String? = null,

    )