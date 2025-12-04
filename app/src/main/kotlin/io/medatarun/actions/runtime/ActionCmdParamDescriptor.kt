package io.medatarun.actions.runtime

import kotlin.reflect.KType

data class ActionCmdParamDescriptor(
    val name: String,
    val type: KType,
    val optional: Boolean
)