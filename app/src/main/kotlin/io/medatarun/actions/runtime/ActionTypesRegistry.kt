package io.medatarun.actions.runtime

import io.medatarun.actions.actions.ActionWithPayload
import io.medatarun.model.domain.AttributeDef
import io.medatarun.model.domain.RelationshipDef
import io.medatarun.model.ports.exposed.AttributeDefUpdateCmd
import io.medatarun.model.ports.exposed.RelationshipDefUpdateCmd
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.TypeDescriptor
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ActionTypesRegistry(private val typeContributions: List<TypeDescriptor<*>>) {

    private val typeDescriptors = typeContributions.associateBy { it.target }

    fun toJsonType(returnType: KType): JsonTypeEquiv {

        val kclass = returnType.classifier as? KClass<*>
        val equivJson = kclass?.let { typeDescriptors[it]?.equivJson }
        if (equivJson != null) return equivJson

        val typeAlias = when (returnType.classifier) {
            String::class -> return JsonTypeEquiv.STRING
            Boolean::class -> return JsonTypeEquiv.BOOLEAN
            Int::class -> return JsonTypeEquiv.NUMBER
            BigInteger::class -> return JsonTypeEquiv.NUMBER
            Double::class -> return JsonTypeEquiv.NUMBER
            BigDecimal::class -> return JsonTypeEquiv.NUMBER
            List::class -> return JsonTypeEquiv.ARRAY
            Map::class -> return JsonTypeEquiv.OBJECT
            Instant::class -> return JsonTypeEquiv.STRING
            LocalDate::class -> return JsonTypeEquiv.STRING

            // TODO those types shall not be here -- do not test that
            ActionWithPayload::class -> return JsonTypeEquiv.OBJECT
            AttributeDef::class -> return JsonTypeEquiv.OBJECT
            AttributeDefUpdateCmd::class -> return JsonTypeEquiv.OBJECT
            RelationshipDef::class -> return JsonTypeEquiv.OBJECT
            RelationshipDefUpdateCmd::class -> return JsonTypeEquiv.OBJECT
            else -> throw UndefinedMultiplatformTypeException(returnType)
        }
        return typeAlias
    }

    fun toMultiplatformType(returnType: KType): String {
        val kclass = returnType.classifier as? KClass<*>
        val equivMultiplatorm = kclass?.let { typeDescriptors[it]?.equivMultiplatorm }
        if (equivMultiplatorm != null) return equivMultiplatorm

        val typeAlias = when (returnType.classifier) {
            String::class -> return "String"
            Boolean::class -> return "Boolean"
            Int::class -> return "Integer"
            BigInteger::class -> "Integer"
            Double::class -> return "Decimal"
            BigDecimal::class -> return "Decimal"
            List::class -> return "List<${toMultiplatformType(returnType.arguments[0].type!!)}>"
            Map::class -> return "Map<${toMultiplatformType(returnType.arguments[0].type!!)},${toMultiplatformType(returnType.arguments[1].type!!)}>"
            Instant::class -> return "Instant"
            LocalDate::class -> return "LocalDate"
            // TODO those types shall not be here -- do not test that
            ActionWithPayload::class -> return "ActionWithPayload"
            AttributeDef::class -> return "AttributeDef"
            AttributeDefUpdateCmd::class -> return "AttributeDefUpdateCmd"
            RelationshipDef::class -> return "RelationshipDef"
            RelationshipDefUpdateCmd::class -> return "RelationshipDefUpdateCmd"
            else -> throw UndefinedMultiplatformTypeException(returnType)
        }
        return typeAlias
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
