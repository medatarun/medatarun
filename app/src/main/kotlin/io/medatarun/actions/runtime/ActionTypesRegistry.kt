package io.medatarun.actions.runtime

import io.medatarun.actions.actions.ActionWithPayload
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.model.domain.AttributeDef
import io.medatarun.model.domain.RelationshipDef
import io.medatarun.model.ports.exposed.AttributeDefUpdateCmd
import io.medatarun.model.ports.exposed.RelationshipDefUpdateCmd
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.TypeDescriptor
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ActionTypesRegistry(private val extensionRegistry: ExtensionRegistry) {
    private val typeContributions = extensionRegistry.findContributionsFlat(TypeDescriptor::class)
    private val typeDescriptors = typeContributions.associateBy { it.target }

    fun toJsonType(returnType: KType): JsonTypeEquiv {

        val kclass = returnType.classifier as? KClass<*>
        val equivJson = kclass?.let { typeDescriptors[it]?.equivJson }
        if (equivJson != null) return equivJson

        val typeAlias = when (returnType.classifier) {
            String::class -> return JsonTypeEquiv.STRING
            Boolean::class -> return JsonTypeEquiv.BOOLEAN
            List::class -> return JsonTypeEquiv.ARRAY
            ActionWithPayload::class -> return JsonTypeEquiv.OBJECT
            Instant::class -> return JsonTypeEquiv.NUMBER
            // TODO shall not be here ???
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
            List::class -> return "List<${toMultiplatformType(returnType.arguments[0].type!!)}>"
            ActionWithPayload::class -> return "ActionWithPayload"
            Instant::class -> return "Instant"
            // TODO shall not be here ???
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