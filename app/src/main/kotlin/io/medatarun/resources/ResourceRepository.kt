package io.medatarun.app.io.medatarun.resources

import io.ktor.http.*
import io.medatarun.cli.AppCLIResources
import io.medatarun.model.model.MedatarunException
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties

class ResourceRepository(private val resources: AppCLIResources) {

    private val resourceDescriptors = AppCLIResources::class.memberProperties
        .filter { it.visibility == KVisibility.PUBLIC }
        .map { ResourceDescriptor(it.name, it, toCommands(it)) }
        .associateBy { it.name }


    private fun toCommands(property: KProperty1<AppCLIResources, *>): List<ResourceCommand> {
        val resourceInstance = property.get(resources) ?: return emptyList()
        val functions = resourceInstance::class.functions
            .filter { it.name !in EXCLUDED_FUNCTIONS }
            .map { function -> buildApiFunctionDescription(function) }
        return functions

    }

    private fun buildApiFunctionDescription(function: KFunction<*>): ResourceCommand = ResourceCommand(
        name = function.name,
        parameters = function.parameters
            .filter { it.kind == KParameter.Kind.VALUE }
            .map { param ->
                ResourceCommandParam(
                    name = param.name ?: "unknown",
                    type = param.type.toString(),
                    optional = (param.isOptional || param.type.isMarkedNullable),
                )
            }
    )

    fun findAllDescriptors(): Collection<ResourceDescriptor> {
        return resourceDescriptors.values
    }

    fun findDescriptorByIdOptional(resourceName: String): ResourceDescriptor? {
        return resourceDescriptors[resourceName]
    }

    fun findResourceInstanceById(resourceName: String): Any? {
        return findDescriptorByIdOptional(resourceName = resourceName)
            ?.property
            ?.get(resources)
            ?: throw ResourceNotFoundException(resourceName)
    }

    data class ResourceDescriptor(
        val name: String,
        val property: KProperty1<AppCLIResources, *>,
        val commands: List<ResourceCommand>,
    )


    data class ResourceCommand(
        val name: String,
        val parameters: List<ResourceCommandParam>
    )


    data class ResourceCommandParam(
        val name: String,
        val type: String,
        val optional: Boolean
    )

    companion object {
        private val EXCLUDED_FUNCTIONS = setOf("equals", "hashCode", "toString")
    }
}

data class ResourceInvocationRequest(
    val resource: String,
    val function: String,
    val parameters: Map<String, String>
)

class ResourceInvocationException(
    val status: HttpStatusCode,
    message: String,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message)

class ResourceNotFoundException(resourceName: String) : MedatarunException("Unknown resource $resourceName")