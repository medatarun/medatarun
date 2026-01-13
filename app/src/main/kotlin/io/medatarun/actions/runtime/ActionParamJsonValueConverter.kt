package io.medatarun.actions.runtime

import io.medatarun.actions.runtime.ActionParametersParser.ConversionResult
import kotlinx.serialization.json.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class ActionParamJsonValueConverter {
    fun convert(raw: JsonElement, classifier: Any?): ConversionResult = when (classifier) {
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
        is KClass<*> -> {
            if (classifier.isValue) {
                val ctor = classifier.primaryConstructor
                    ?: return ConversionResult.Error("No constructor for value class ${classifier.simpleName}")

                val innerParam = ctor.parameters.single()
                when (val inner = convert(raw, innerParam.type.classifier)) {
                    is ConversionResult.Value ->
                        ConversionResult.Value(ctor.call(inner.value))

                    is ConversionResult.Error ->
                        inner
                }
            } else if (classifier.isData) {
                val ctor = classifier.primaryConstructor
                if (ctor == null) {
                    ConversionResult.Error("No primary constructor for data class ${classifier.simpleName}")
                } else {
                    val obj = raw.jsonObject
                    val args = mutableMapOf<KParameter, Any?>()
                    var error: ConversionResult.Error? = null

                    for (param in ctor.parameters) {
                        val field = obj[param.name]
                        if (field == null) {
                            error = ConversionResult.Error("Missing field '${param.name}' for ${classifier.simpleName}")
                            break
                        }

                        val converted = convert(field, param.type.classifier)
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
                ConversionResult.Error("Unsupported parameter type: ${classifier.simpleName}")
            }
        }

        else -> ConversionResult.Value(raw)
    }

}