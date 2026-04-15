package io.medatarun.ext.modeljson.internal.v3

import io.medatarun.ext.modeljson.internal.base.ModelAttributeJson
import io.medatarun.ext.modeljson.internal.base.RelationshipRoleJson
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class RelationshipJsonV3(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val name: String? = null,
    val description: String? = null,
    val roles: List<RelationshipRoleJsonV3>,
    val attributes: List<ModelAttributeJsonV3>? = emptyList(),
    val tags: List<String>? = emptyList()
)