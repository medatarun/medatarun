package io.medatarun.httpserver.rest

import io.medatarun.actions.runtime.ActionRegistry
import kotlinx.serialization.Serializable

class RestApiDoc(private val actionRegistry: ActionRegistry) {


    fun buildApiDescription(): Map<String, List<ApiDescriptionFunction>> {
        return actionRegistry
            .findAllGroupDescriptors().associate { res ->
                res.key to res.actions.map { actionKey ->
                    ApiDescriptionFunction(
                        key = actionKey.key,
                        title = actionKey.title ?: actionKey.key,
                        description = actionKey.description,
                        parameters = actionKey.parameters.map { p ->
                            ApiDescriptionParam(
                                name = p.name,
                                type = p.multiplatformType,
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
    val key: String,
    val title: String,
    val description: String?,
    val parameters: List<ApiDescriptionParam>
)

@Serializable
data class ApiDescriptionParam(val name: String, val type: String, val optional: Boolean)
