package io.medatarun.httpserver.rest

import io.medatarun.resources.ResourceRepository
import kotlinx.serialization.Serializable

class RestApiDoc(private val resourceRepository: ResourceRepository) {


    fun buildApiDescription(): Map<String, List<ApiDescriptionFunction>> {
        return resourceRepository
            .findAllDescriptors().associate { res ->
                res.name to res.commands.map { cmd ->
                    ApiDescriptionFunction(
                        name = cmd.name,
                        title = cmd.title ?: cmd.name,
                        description = cmd.description,
                        parameters = cmd.parameters.map { p ->
                            ApiDescriptionParam(
                                name = p.name,
                                type = p.type.toString(),
                                optional = p.optional
                            )
                        }
                    )
                }
            }
    }
}

@Serializable
data class ApiDescriptionFunction(
    val name: String,
    val title: String,
    val description: String?,
    val parameters: List<ApiDescriptionParam>
)

@Serializable
data class ApiDescriptionParam(val name: String, val type: String, val optional: Boolean)
