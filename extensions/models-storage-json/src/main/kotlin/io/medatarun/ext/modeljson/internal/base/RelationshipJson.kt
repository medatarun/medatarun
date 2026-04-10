package io.medatarun.ext.modeljson.internal.base

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class RelationshipJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val roles: List<RelationshipRoleJson>,
    val attributes: List<ModelAttributeJson>,
    val tags: List<String>? = emptyList()
)