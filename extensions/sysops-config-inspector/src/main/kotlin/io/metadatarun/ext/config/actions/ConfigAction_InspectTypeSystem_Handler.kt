package io.metadatarun.ext.config.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.types.TypeDescriptor
import kotlinx.serialization.Serializable

class ConfigAction_InspectTypeSystem_Handler(
    private val extensionRegistry: ExtensionRegistry
) {
    fun run(action: ConfigAction.InspectTypeSystem, actionCtx: ActionCtx): InspectTypeSystemResp {
        val contribs = extensionRegistry.findContributionsFlat(TypeDescriptor::class)
        return InspectTypeSystemResp(
            items = contribs.map {
                TypeDescriptorDto(
                    id = it.equivMultiplatorm,
                    equivJson = it.equivJson.code,
                    description = it.description
                )
            }
        )
    }
}

@Serializable
data class InspectTypeSystemResp(val items: List<TypeDescriptorDto>)

@Serializable
data class TypeDescriptorDto(
    val id: String,
    val equivJson: String,
    val description: String
)