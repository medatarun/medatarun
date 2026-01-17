package io.medatarun.actions.runtime

import io.medatarun.actions.actions.ActionWithPayload
import io.medatarun.types.TypeDescriptor
import io.medatarun.types.TypeJsonEquiv
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ActionTypesRegistry(typeContributions: List<TypeDescriptor<*>>) {

    private val typeDescriptors = typeContributions.associateBy { it.target }

    fun toJsonType(returnType: KType): TypeJsonEquiv {

        val classifier = returnType.classifier
        val kclass = classifier as? KClass<*>
        val equivJson = kclass?.let { typeDescriptors[it]?.equivJson }
        if (equivJson != null) return equivJson

        when (classifier) {
            String::class -> return TypeJsonEquiv.STRING
            Boolean::class -> return TypeJsonEquiv.BOOLEAN
            Int::class -> return TypeJsonEquiv.NUMBER
            BigInteger::class -> return TypeJsonEquiv.NUMBER
            Double::class -> return TypeJsonEquiv.NUMBER
            BigDecimal::class -> return TypeJsonEquiv.NUMBER
            List::class -> return TypeJsonEquiv.ARRAY
            Map::class -> return TypeJsonEquiv.OBJECT
            Instant::class -> return TypeJsonEquiv.STRING
            LocalDate::class -> return TypeJsonEquiv.STRING

            // TODO those types shall not be here -- do not test that
            ActionWithPayload::class -> return TypeJsonEquiv.OBJECT
            else -> throw UndefinedMultiplatformTypeException(returnType)
        }

    }

    fun toMultiplatformType(returnType: KType): String {
        val kclass = returnType.classifier as? KClass<*>
        val equivMultiplatorm = kclass?.let { typeDescriptors[it]?.equivMultiplatorm }
        if (equivMultiplatorm != null) return equivMultiplatorm

        when (returnType.classifier) {
            String::class -> return "String"
            Boolean::class -> return "Boolean"
            Int::class -> return "Integer"
            BigInteger::class -> return "Integer"
            Double::class -> return "Decimal"
            BigDecimal::class -> return "Decimal"
            List::class -> return "List<${toMultiplatformType(returnType.arguments[0].type!!)}>"
            Map::class -> return "Map<${toMultiplatformType(returnType.arguments[0].type!!)},${toMultiplatformType(returnType.arguments[1].type!!)}>"
            Instant::class -> return "Instant"
            LocalDate::class -> return "LocalDate"
            // TODO those types shall not be here -- do not test that
            ActionWithPayload::class -> return "ActionWithPayload"
            else -> throw UndefinedMultiplatformTypeException(returnType)
        }

    }



    fun findValidator(classifier: KClass<*>): Validator {
        val typeDescriptor = typeDescriptors[classifier] ?: return ValidatorNoop
        return ValidatorDelegate(typeDescriptor)
    }

    interface Validator {
        fun validate(value: Any): Any
    }

    object ValidatorNoop : Validator {
        override fun validate(value: Any): Any {
            return value
        }
    }

    class ValidatorDelegate(private val typeDescriptor: TypeDescriptor<*>) : Validator {
        override fun validate(value: Any): Any {
            return (typeDescriptor as TypeDescriptor<Any>).validate(value)
        }
    }



}
