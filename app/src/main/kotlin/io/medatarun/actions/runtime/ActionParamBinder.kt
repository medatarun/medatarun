package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.actions.ports.needs.ActionProvider
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class ActionParamBinder(private val actionTypesRegistry: ActionTypesRegistry) {

    private val jsonValueConverter = ActionParamJsonValueConverter(actionTypesRegistry)


    fun buildConstructorArgs(
        actionClass: KClass<out Any>,
        actionProviderInstance: ActionProvider<Any>,
        actionPayload: JsonObject
    ): ActionParamBindings {
        val callArgs = mutableMapOf<KParameter, ActionParamBindingState>()

        val function = actionClass.primaryConstructor ?: throw ActionInvocationException(
            HttpStatusCode.InternalServerError,
            "Action class $actionClass has no primary constructor"
        )
        function.parameters.forEach { parameter ->
            when (parameter.kind) {
                // Action classes are inner classes of ActionProvider(s).
                // Kotlin reflection represents the outer instance as a synthetic INSTANCE parameter.
                // When calling the constructor of the action via callBy(), this outer instance must be provided,
                // otherwise the reflective call fails.
                KParameter.Kind.INSTANCE -> callArgs[parameter] = ActionParamBindingState.Ok(actionProviderInstance)
                KParameter.Kind.VALUE -> {
                    val paramSerialName = parameter.name ?: throw ActionInvocationException(
                        HttpStatusCode.InternalServerError,
                        "Parameter [${parameter}] has no name"
                    )

                    if (!actionPayload.containsKey(paramSerialName)) {
                        if (!parameter.isOptional) {
                            callArgs[parameter] = ActionParamBindingState.Missing(HttpStatusCode.BadRequest)
                        } else {
                            callArgs[parameter] = ActionParamBindingState.Ok(null)
                        }
                    } else {
                        val raw = actionPayload.get(paramSerialName) ?: throw ActionInvocationException(
                            HttpStatusCode.InternalServerError,
                            "Parameter [${paramSerialName}] could not be found in Json payload"
                        )

                        when (val conversion = jsonValueConverter.convert(raw, parameter.type)) {
                            is ActionParamJsonValueConverter.ConversionResult.Error ->
                                callArgs[parameter] = ActionParamBindingState.Error(HttpStatusCode.BadRequest, conversion.message)

                            is ActionParamJsonValueConverter.ConversionResult.Value -> {
                                try {
                                    // If value is null, nothing to validate
                                    if (conversion.value != null) {
                                        // Additional syntaxical validation based on typeregistry
                                        // (not business validation, just "is this correctly written")
                                        validate(parameter, conversion.value)
                                    }
                                    callArgs[parameter] = ActionParamBindingState.Ok(conversion.value)
                                } catch (e: Throwable) {
                                    callArgs[parameter] = ActionParamBindingState.Error(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
                                }

                            }
                        }

                    }
                }

                else -> {}
            }
        }
        return ActionParamBindings(callArgs)
    }

    private fun validate(parameter: KParameter, value: Any?): Any? {
        val classifier = parameter.type.classifier as? KClass<*>

        if (value != null && classifier != null) {
            val validator = actionTypesRegistry.findValidator(classifier)
            return validator.validate(value)
        } else {
            return value
        }
    }

}