package io.medatarun.ext.modeljson.internal.v3

import io.medatarun.ext.modeljson.internal.base.ModelTypeJson
import io.medatarun.ext.modeljson.internal.base.RelationshipJson
import io.medatarun.ext.modeljson.internal.v2.ModelEntityJsonV2
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import kotlinx.serialization.Contextual
import kotlinx.serialization.EncodeDefault
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
    val name: @Contextual LocalizedText? = null,
    val description: @Contextual LocalizedMarkdown? = null,
    val origin: String? = null,
    val authority: String? = null,
    val tags: List<String>? = emptyList(),
    val types: List<ModelTypeJson>? = emptyList(),
    val entities: List<ModelEntityJsonV3>? = emptyList(),
    val relationships: List<RelationshipJson> = emptyList(),
    val documentationHome: String? = null,
    val businessKeys: List<BusinessKeyJsonV3>? = emptyList()
    )
