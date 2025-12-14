package io.medatarun.actions.runtime

import kotlin.reflect.KType

data class ActionCmdParamDescriptor(
    val name: String,
    val title: String?,
    val type: KType,
    val optional: Boolean,
    val order: Int,
    val description: String?,
)