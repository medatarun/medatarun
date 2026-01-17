package io.medatarun.actions.runtime

import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KType

data class ActionCmdParamDescriptor(
    val name: String,
    val title: String?,
    val type: KType,
    val multiplatformType: String,
    val jsonType: TypeJsonEquiv,
    val optional: Boolean,
    val order: Int,
    val description: String?,
)