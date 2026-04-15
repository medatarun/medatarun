package io.medatarun.ext.modeljson.internal.v3

import kotlinx.serialization.Serializable

@Serializable
internal class RelationshipRoleJsonV3(
    /** Note that for imports, this may be null */
    val id: String? = null,
    val key: String,
    val entity: String,
    val name: String? = null,
    val cardinality: String
)