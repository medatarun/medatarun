package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class ActionParamJsonValueConverter(
    private val typeConverters: ActionTypesRegistry,
) {
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

        val converter: TypeJsonConverter<*>? = typeConverters.findConverterOptional(classifier)

        return when {
            converter != null -> convertWithConverter(raw, converter)
            classifier == List::class -> convertList(raw, type)
            classifier == Map::class -> convertMap(raw, type)
            else -> convertScalar(raw, type)
        }

    }

    private fun convertWithConverter(
        raw: JsonElement,
        converter: TypeJsonConverter<*>
    ): ConversionResult {
        try {
            val value = converter.deserialize(raw)
            return ConversionResult.Value(value)
        } catch (e: TypeJsonConverterIllegalNullException) {
            throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                e.msg
            )
        } catch (e: TypeJsonConverterBadFormatException) {
            return ConversionResult.Error(e.msg)
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

        return when (kclass) {
            Int::class -> convertInt(raw)
            Boolean::class -> convertBoolean(raw)
            String::class -> convertString(raw)
            BigDecimal::class -> convertBigDecimal(raw)
            BigInteger::class -> convertBigInteger(raw)
            Double::class -> convertDouble(raw)
            Instant::class -> convertInstant(raw)
            LocalDate::class -> convertLocalDate(raw)
            UUID::class -> convertUuid(raw)
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
    }

    private fun convertInt(raw: JsonElement): ConversionResult {
        return runCatching { raw.jsonPrimitive.int }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting Int cannot parse value '$raw'") }
            )
    }

    private fun convertBoolean(raw: JsonElement): ConversionResult {
        return runCatching { raw.jsonPrimitive.boolean }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting Boolean cannot parse value '$raw'") }
            )
    }

    private fun convertString(raw: JsonElement): ConversionResult {
        if (raw is JsonNull) return ConversionResult.Value(null)
        return runCatching { raw.jsonPrimitive.content }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting String cannot parse value '$raw'") }
            )
    }

    private fun convertBigDecimal(raw: JsonElement): ConversionResult {
        return runCatching { BigDecimal(raw.jsonPrimitive.content) }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting BigDecimal cannot parse value '$raw'") }
            )
    }

    private fun convertBigInteger(raw: JsonElement): ConversionResult {
        return runCatching { BigInteger(raw.jsonPrimitive.content) }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting BigInteger cannot parse value '$raw'") }
            )
    }

    private fun convertDouble(raw: JsonElement): ConversionResult {
        return runCatching { raw.jsonPrimitive.content.toDouble() }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting Double cannot parse value '$raw'") }
            )
    }

    private fun convertInstant(raw: JsonElement): ConversionResult {
        return runCatching {
            val content = raw.jsonPrimitive.content
            val epochMilli = content.toLongOrNull()
            if (epochMilli != null) {
                Instant.ofEpochMilli(epochMilli)
            } else {
                Instant.parse(content)
            }
        }.fold(
            onSuccess = { ConversionResult.Value(it) },
            onFailure = { ConversionResult.Error("Parameter expecting Instant cannot parse value '$raw'") }
        )
    }

    private fun convertLocalDate(raw: JsonElement): ConversionResult {
        return runCatching { LocalDate.parse(raw.jsonPrimitive.content) }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting LocalDate cannot parse value '$raw'") }
            )
    }

    private fun convertUuid(raw: JsonElement): ConversionResult {
        return runCatching { UuidUtils.fromString(raw.jsonPrimitive.content) }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting UUID cannot parse value '$raw'") }
            )
    }

    fun convertMap(raw: JsonElement, type: KType): ConversionResult {
        val keyType = type.arguments.getOrNull(0)?.type
            ?: throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "Map type has no key generic argument"
            )
        val valueType = type.arguments.getOrNull(1)?.type
            ?: throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "Map type has no value generic argument"
            )

        val obj = raw as? JsonObject
            ?: return ConversionResult.Error("Expected JSON object but got $raw")

        val values = mutableMapOf<Any?, Any?>()
        for (entry in obj) {
            val keyElement = JsonPrimitive(entry.key)
            val convertedKey = convert(keyElement, keyType)
            val convertedValue = convert(entry.value, valueType)
            when {
                convertedKey is ConversionResult.Error -> return convertedKey
                convertedValue is ConversionResult.Error -> return convertedValue
                convertedKey is ConversionResult.Value && convertedValue is ConversionResult.Value -> {
                    values[convertedKey.value] = convertedValue.value
                }
            }
        }
        return ConversionResult.Value(values)
    }

    private fun convertValueClass(
        kclass: KClass<*>,
        raw: JsonElement
    ): ConversionResult {

        // Gets the value class constructor and its only parameter
        val ctor = kclass.primaryConstructor
            ?: throw ActionInvocationException(
                HttpStatusCode.InternalServerError,
                "No constructor for value class ${kclass.simpleName}"
            )
        val innerParam = ctor.parameters.single()

        // The general rule is: you never create a value class with "null" inside.
        // when JSON is a null, then the value will be null whatever
        // when the conversion of the param is null, then the value will be null whatever
        // otherwise we accept the converted value and use it as the "value class" first and only parameter
        return if (raw == JsonNull) {
            ConversionResult.Value(null)
        } else {
            val inner = convert(raw, innerParam.type)
            when (inner) {
                is ConversionResult.Value -> if (inner.value == null) {
                    ConversionResult.Value(null)
                } else {
                    ConversionResult.Value(ctor.call(inner.value))
                }

                is ConversionResult.Error -> inner
            }
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
