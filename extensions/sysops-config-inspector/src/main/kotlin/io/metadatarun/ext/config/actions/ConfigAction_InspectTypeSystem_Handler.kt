package io.metadatarun.ext.config.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.type.commons.enums.EnumWithCode
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
                    description = it.description,
                    enumValues = enumValues(it)
                )
            }
        )
    }

    private fun enumValues(typeDescriptor: TypeDescriptor<*>): List<String>? {
        val targetJavaClass = typeDescriptor.target.java
        if (!targetJavaClass.isEnum) {
            return null
        }

        val enumConstants = targetJavaClass.enumConstants
        if (EnumWithCode::class.java.isAssignableFrom(targetJavaClass)) {
            return enumConstants.map { enumConstant ->
                (enumConstant as EnumWithCode).code
            }
        }

        return enumConstants.map { enumConstant ->
            (enumConstant as Enum<*>).name
        }
    }
}

@Serializable
data class InspectTypeSystemResp(val items: List<TypeDescriptorDto>)

@Serializable
data class TypeDescriptorDto(
    val id: String,
    val equivJson: String,
    val description: String,
    val enumValues: List<String>?
)
