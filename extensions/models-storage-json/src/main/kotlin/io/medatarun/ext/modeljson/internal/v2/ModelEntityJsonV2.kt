package io.medatarun.ext.modeljson.internal.v2

import io.medatarun.ext.modeljson.internal.base.ModelAttributeJson
import io.medatarun.ext.modeljson.internal.serializers.LocalizedTextMultiLangCompat
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class ModelEntityJsonV2(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedTextMultiLangCompat? = null,
    val description: @Contextual LocalizedTextMultiLangCompat? = null,
    val identifierAttribute: @Contextual String,
    val origin: String? = null,
    val tags: List<String>? = emptyList(),
    val attributes: List<ModelAttributeJson>,
    val documentationHome: String? = null,

    )