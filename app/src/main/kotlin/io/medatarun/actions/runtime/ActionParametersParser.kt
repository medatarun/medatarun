package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionProvider
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class ActionParametersParser(private val actionTypesRegistry: ActionTypesRegistry) {

    private val jsonValueConverter = ActionParamJsonValueConverter()

    sealed interface ConversionResult {
        data class Value(val value: Any?) : ConversionResult
        data class Error(val message: String) : ConversionResult
    }

    data class BuildCallArgsResult(
        val callArgs: Map<KParameter, Any?>,
        val conversionErrors: List<String>
    )

    fun buildCallArgs(actionCommandFunction: KFunction<*>, actionProviderInstance: ActionProvider<Any>, actionPayload: JsonObject): BuildCallArgsResult {
        val callArgs = mutableMapOf<KParameter, Any?>()
        val conversionErrors = mutableListOf<String>()

        actionCommandFunction.parameters.forEach { parameter ->
            when (parameter.kind) {
                KParameter.Kind.INSTANCE -> callArgs[parameter] = actionProviderInstance
                KParameter.Kind.VALUE -> {
                    val raw = parameter.name?.let(actionPayload::get)
                    if (raw != null) {
                        when (val conversion = jsonValueConverter.convert(raw, parameter.type.classifier)) {
                            is ConversionResult.Error -> conversionErrors += conversion.message
                            is ConversionResult.Value -> callArgs[parameter] = try {
                                validate(parameter, conversion.value)
                            } catch (err: Throwable) {
                                conversionErrors += "Invalid value for ${parameter.name}. " + (err.message ?: "")
                            }

                        }
                    }
                }

                else -> Unit
            }
        }
        return BuildCallArgsResult(callArgs, conversionErrors)
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