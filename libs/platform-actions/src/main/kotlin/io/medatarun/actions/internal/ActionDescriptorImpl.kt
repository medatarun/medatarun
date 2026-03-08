package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
import kotlin.reflect.KType

data class ActionDescriptorImpl(
    val base: ActionDescriptorBase,
    val params: List<ActionParamDescriptorImpl>,
    override val semantics: ActionSemanticsConfig
) : ActionCmdDescriptor {
    override val id: ActionId = base.id
    override val key: String = base.key
    override val actionClassName: String = base.actionClassName
    override val group: String = base.group
    override val title: String? = base.title
    override val description: String? = base.description
    override val resultType: KType = base.resultType
    override val parameters: List<ActionCmdParamDescriptor> = params
    override val accessType: ActionCmdAccessType = base.accessType
    override val uiLocations: Set<String> = base.uiLocations
    override val securityRule: String = base.securityRule
}