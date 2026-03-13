package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionDescriptorParam
import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KType

internal data class ActionDescriptorParamImpl(
    override val key: String,
    override val title: String?,
    override val type: KType,
    override val multiplatformType: String,
    override val jsonType: TypeJsonEquiv,
    override val optional: Boolean,
    override val order: Int,
    override val description: String?,
) : ActionDescriptorParam