package io.medatarun.ext.modeljson.internal.base

import io.medatarun.ext.modeljson.internal.serializers.LocalizedTextMultiLangCompat
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class RelationshipRoleJson(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val entityId: String,
    val name: @Contextual LocalizedTextMultiLangCompat? = null,
    val cardinality: String
)