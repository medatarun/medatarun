package io.medatarun.ext.modeljson.internal.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class ModelJsonV3(
    /** Note that for imports, id may be null or missing.*/
    val id: String? = null,
    val key: String,
    @SerialName($$"$schema")
    val schema: String,
    val version: String,
    val name: String? = null,
    val description: String? = null,
    val origin: String? = null,
    val authority: String? = null,
    val tags: List<String>? = emptyList(),
    val types: List<ModelTypeJsonV3>? = emptyList(),
    val entities: List<ModelEntityJsonV3>? = emptyList(),
    val relationships: List<RelationshipJsonV3> = emptyList(),
    val documentationHome: String? = null,
    val businessKeys: List<BusinessKeyJsonV3>? = emptyList()
    )
