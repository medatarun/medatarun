package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionPayload
import io.medatarun.actions.ports.needs.ActionProvider
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

internal class ActionPayloadDeserializerFromProviders(
    val actionProviderInstance: ActionProvider<Any>,
    val actionTypesRegistry: ActionTypesRegistry
) : ActionPayloadJsonDeserializer {

    private val actionParamBinder = ActionParamBinder(actionTypesRegistry)

    override fun deserialize(action: ActionRegistered, payload: ActionPayload): Any {
        return when (payload) {
            is ActionPayload.AsJson -> deserializePayloadJson(action, payload.value)
            is ActionPayload.AsRaw -> deserializePayloadRaw(action, payload.value)
        }
    }

    private fun deserializePayloadRaw(
        action: ActionRegistered,
        actionPayload: Any
    ): Any {
        val groupKey = actionProviderInstance.actionGroupKey
        val actionKey = actionPayload::class.findAnnotation<ActionDoc>()?.key
        val correct = groupKey == action.descriptor.group
                && actionKey == action.descriptor.key
                && findActionClassFromDescriptorClass(action) == actionPayload::class
        if (!correct) throw ActionInvocationDeserializePayloadRawMismatchException(
            actionPayload::class, action.descriptor.group, action.descriptor.key
        )
        return actionPayload
    }

    private fun findActionClassFromDescriptorClass(action: ActionRegistered): KClass<out Any> {
        return actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.firstOrNull { it.simpleName == action.descriptor.actionClassName }
            ?: throw ActionInvocationClassFromDescriptorNotFoundException(
                action.descriptor.group,
                action.descriptor.key,
                action.descriptor.actionClassName
            )
    }


    private fun deserializePayloadJson(
        action: ActionRegistered,
        actionPayload: JsonObject
    ): Any {
        val actionDescriptor = action.descriptor
        val actionGroupKey = actionDescriptor.group
        val actionKey = actionDescriptor.key
        val actionClass = actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.firstOrNull { it.simpleName == actionDescriptor.actionClassName }
            ?: throw ActionInvocationNotFoundException(actionGroupKey, actionKey)

        val bindings = actionParamBinder.buildConstructorArgs(
            actionClass = actionClass,
            actionProviderInstance = actionProviderInstance,
            actionPayload = actionPayload,
            actionDescriptor = actionDescriptor
        )

        // This will throw exceptions if invalid parameters exists (missing, with errors, etc.)
        bindings.technicalValidation()

        // Now we can assume there is no error anymore on parameters
        val cmd = actionClass.primaryConstructor?.callBy(bindings.toCallArgs())
            ?: throw ActionInvocationClassHasNoPrimaryConstructorException(actionClass)
        return cmd
    }

}