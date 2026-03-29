package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.actions.domain.ActionRegistered
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionPayload
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.lang.http.StatusCode
import kotlinx.serialization.json.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        if (!correct) throw ActionInvocationException(
            StatusCode.NOT_FOUND,
            "Action payload class ${actionPayload::class} does't match expected payload for ${action.descriptor.group}/${action.descriptor.key}."
        )
        return actionPayload
    }

    private fun findActionClassFromDescriptorClass(action: ActionRegistered): KClass<out Any> {
        return actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.firstOrNull { it.simpleName == action.descriptor.actionClassName }
            ?: throw ActionInvocationException(
                StatusCode.NOT_FOUND,
                "Action ${action.descriptor.group}/${action.descriptor.key} not found"
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
            ?: throw ActionInvocationException(
                StatusCode.NOT_FOUND,
                "Action $actionGroupKey/$actionKey not found"
            )

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
            ?: throw ActionInvocationException(
                StatusCode.INTERNAL_SERVER_ERROR,
                "Action class $actionClass has no primary constructor"
            )
        return cmd
    }

}