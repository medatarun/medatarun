package io.medatarun.ext.modeljson.internal.v3

import io.medatarun.ext.modeljson.internal.base.ModelAttributeJson
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class ModelEntityJsonV3(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: String? = null,
    val description: String? = null,
    val origin: String? = null,
    val tags: List<String>? = emptyList(),
    val attributes: List<ModelAttributeJsonV3>? = emptyList(),
    val documentationHome: String? = null,
    /** Attribute identifier ids that participate in the primary key */
    val primaryKey: List<String>? = emptyList()
    )