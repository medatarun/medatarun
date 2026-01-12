package io.medatarun.actions.runtime

import io.medatarun.types.JsonTypeEquiv
import kotlin.reflect.KType

data class ActionCmdParamDescriptor(
    val name: String,
    val title: String?,
    val type: KType,
    val multiplatformType: String,
    val jsonType: JsonTypeEquiv,
    val optional: Boolean,
    val order: Int,
    val description: String?,
)