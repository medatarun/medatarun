package io.medatarun.actions.domain

import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KType

interface ActionCmdParamDescriptor {
    val name: String
    val title: String?
    val type: KType
    val multiplatformType: String
    val jsonType: TypeJsonEquiv
    val optional: Boolean
    val order: Int
    val description: String?
}

data class ActionParamDescriptorImpl(
    override val name: String,
    override val title: String?,
    override val type: KType,
    override val multiplatformType: String,
    override val jsonType: TypeJsonEquiv,
    override val optional: Boolean,
    override val order: Int,
    override val description: String?,
) : ActionCmdParamDescriptor