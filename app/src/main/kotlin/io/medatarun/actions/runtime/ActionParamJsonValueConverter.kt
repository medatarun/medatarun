package io.medatarun.actions.runtime

import io.ktor.http.*
import kotlinx.serialization.json.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class ActionParamJsonValueConverter {
    sealed interface ConversionResult {
        data class Value(val value: Any?) : ConversionResult
        data class Error(val message: String) : ConversionResult
    }

    fun convert(raw: JsonElement, type: KType): ConversionResult {

        // Specific case for generics

        val classifier = type.classifier as? KClass<*>
            ?: throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "Unsupported type $type"
            )

        return when {
            classifier == List::class -> convertList(raw, type)
            else -> convertScalar(raw, type)
        }

    }

    fun convertList(raw: JsonElement, type: KType): ConversionResult {
        val elementType = type.arguments.singleOrNull()?.type
            ?: throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "List type has no generic argument"
            )

        val array = raw as? JsonArray
            ?: return ConversionResult.Error("Expected JSON array but got $raw")

        val values = mutableListOf<Any?>()

        for (element in array) {
            when (val converted = convert(element, elementType)) {
                is ConversionResult.Error -> return converted
                is ConversionResult.Value -> values += converted.value
            }
        }

        return ConversionResult.Value(values)
    }

    fun convertScalar(raw: JsonElement, type: KType): ConversionResult {

        val kclass = type.classifier as? KClass<*>
            ?: throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "Can not manage KType of kind [$type]"
            )

        val result = when (kclass) {
            Int::class -> runCatching { raw.jsonPrimitive.int }
                .fold(
                    onSuccess = { ConversionResult.Value(it) },
                    onFailure = { ConversionResult.Error("Parameter expecting Int cannot parse value '$raw'") }
                )

            Boolean::class -> runCatching { ConversionResult.Value(raw.jsonPrimitive.boolean) }
                .fold(
                    onSuccess = { ConversionResult.Value(it) },
                    onFailure = { ConversionResult.Error("Parameter expecting Boolean cannot parse value '$raw'") }
                )

            String::class -> runCatching { ConversionResult.Value(raw.jsonPrimitive.content) }
                .fold(
                    onSuccess = { ConversionResult.Value(it) },
                    onFailure = { ConversionResult.Error("Parameter expecting String cannot parse value '$raw'") }
                )

            UUID::class -> runCatching { UUID.fromString(raw.jsonPrimitive.content) }
                .fold(
                    onSuccess = { ConversionResult.Value(it) },
                    onFailure = { ConversionResult.Error("Parameter expecting UUID cannot parse value '$raw'") }
                )

            else -> {
                if (kclass.isValue) {
                    convertValueClass(kclass, raw)
                } else if (kclass.isData) {
                    convertDataClass(kclass, raw)
                } else {
                    throw ActionInvocationException(
                        HttpStatusCode.InternalServerError,
                        "Unsupported parameter type: ${kclass.simpleName}"
                    )
                }
            }


        }
        return result
    }

    private fun convertValueClass(
        kclass: KClass<*>,
        raw: JsonElement
    ): ConversionResult {
        val ctor = kclass.primaryConstructor
            ?: throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "No constructor for value class ${kclass.simpleName}"
            )

        val innerParam = ctor.parameters.single()
        return when (val inner = convert(raw, innerParam.type)) {
            is ConversionResult.Value ->
                ConversionResult.Value(ctor.call(inner.value))

            is ConversionResult.Error -> inner
        }
    }

    private fun convertDataClass(
        kclass: KClass<*>,
        raw: JsonElement
    ): ConversionResult {
        val ctor = kclass.primaryConstructor
        return if (ctor == null) {
            throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "No primary constructor for data class ${kclass.simpleName}"
            )
        } else {

            if (raw is JsonObject) {
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
            } else {
                ConversionResult.Error("Parameter should be a JsonObject. Cannot parse value '$raw'")
            }
        }
    }

}