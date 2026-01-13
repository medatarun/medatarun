package io.medatarun.actions.runtime

import io.medatarun.actions.runtime.ActionParametersParser.ConversionResult
import kotlinx.serialization.json.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class ActionParamJsonValueConverter {
    fun convert(raw: JsonElement, type: KType): ConversionResult {
        val kclass = type.classifier as? KClass<*> ?: return ConversionResult.Error("Can not manage KType of kind [$type]")
        val result = when (kclass) {
            Int::class -> runCatching { raw.jsonPrimitive.int }
                .fold(
                    onSuccess = { ConversionResult.Value(it) },
                    onFailure = { ConversionResult.Error("Parameter expecting Int cannot parse value '$raw'") }
                )

            Boolean::class -> ConversionResult.Value(raw.jsonPrimitive.boolean)
            String::class -> ConversionResult.Value(raw.jsonPrimitive.content)
            UUID::class -> runCatching { UUID.fromString(raw.jsonPrimitive.content) }
                .fold(
                    onSuccess = { ConversionResult.Value(it) },
                    onFailure = { ConversionResult.Error("Parameter expecting UUID cannot parse value '$raw'") }
                )

            else -> {
                if (kclass.isValue) {
                    val ctor = kclass.primaryConstructor
                        ?: return ConversionResult.Error("No constructor for value class ${kclass.simpleName}")

                    val innerParam = ctor.parameters.single()
                    when (val inner = convert(raw, innerParam.type)) {
                        is ConversionResult.Value ->
                            ConversionResult.Value(ctor.call(inner.value))

                        is ConversionResult.Error ->
                            inner
                    }
                } else if (kclass.isData) {
                    val ctor = kclass.primaryConstructor
                    if (ctor == null) {
                        ConversionResult.Error("No primary constructor for data class ${kclass.simpleName}")
                    } else {
                        val obj = raw.jsonObject
                        val args = mutableMapOf<KParameter, Any?>()
                        var error: ConversionResult.Error? = null

                        for (param in ctor.parameters) {
                            val field = obj[param.name]
                            if (field == null) {
                                error =
                                    ConversionResult.Error("Missing field '${param.name}' for ${kclass.simpleName}")
                                break
                            }

                            val converted = convert(field, param.type)
                            if (converted is ConversionResult.Value)
                                args[param] = converted.value
                            else if (converted is ConversionResult.Error) {
                                error = converted
                                break
                            }
                        }

                        if (error != null) error else ConversionResult.Value(ctor.callBy(args))
                    }
                } else {
                    ConversionResult.Error("Unsupported parameter type: ${kclass.simpleName}")
                }
            }


        }
        return result
    }

}