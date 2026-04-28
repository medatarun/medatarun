package io.medatarun.ext.modeljson.internal.v2

import io.medatarun.ext.modeljson.internal.base.ModelTypeJson
import io.medatarun.ext.modeljson.internal.base.RelationshipJson
import io.medatarun.model.domain.TextMarkdown
import io.medatarun.model.domain.TextSingleLine
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class ModelJsonV2(
    /** Note that for imports, id may be null or missing.*/
    val id: String? = null,
    val key: String,
    @SerialName($$"$schema")
    val schema: String,
    val version: String,
    val name: @Contextual TextSingleLine? = null,
    val description: @Contextual TextMarkdown? = null,
    val origin: String? = null,
    val authority: String? = null,
    val tags: List<String>? = emptyList(),
    val types: List<ModelTypeJson>,
    val entities: List<ModelEntityJsonV2>,
    val relationships: List<RelationshipJson> = emptyList(),
    val documentationHome: String? = null,

    )