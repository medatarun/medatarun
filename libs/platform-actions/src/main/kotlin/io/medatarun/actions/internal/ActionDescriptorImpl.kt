package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
import kotlin.reflect.KType

internal data class ActionDescriptorImpl(
    val base: ActionDescriptorBase,
    val params: List<ActionDescriptorParamImpl>,
    override val semantics: ActionSemanticsConfig
) : ActionDescriptor {
    override val id: ActionId = base.id
    override val key: String = base.key
    override val actionClassName: String = base.actionClassName
    override val group: String = base.group
    override val title: String? = base.title
    override val description: String? = base.description
    override val resultType: KType = base.resultType
    override val parameters: List<ActionDescriptorParam> = params
    override val accessType: ActionAccessType = base.accessType
    override val securityRule: String = base.securityRule
}