package io.medatarun.ext.datamdfile.internal

import io.medatarun.model.model.MedatarunException
import io.medatarun.model.model.ModelAttributeId
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId

class MdFileEntityDefinitionNotManagedException(
    modelId: ModelId,
    entityDefId: ModelEntityId
) : MedatarunException(
    "MdFileDataRepository does not manage entity definition '${entityDefId.value}' for model '${modelId.value}'"
)

class MdFileEntityIdMissingException(
    entityDefId: ModelEntityId
) : MedatarunException(
    "Markdown entity for definition '${entityDefId.value}' must define attribute 'id'"
)

class MdFileEntityNotFoundException(
    entityDefId: ModelEntityId,
    instanceId: String
) : MedatarunException(
    "Markdown entity '${instanceId}' not found for definition '${entityDefId.value}'"
)

class MdFileEntityAttributeUnknownException(
    entityDefId: ModelEntityId,
    attributeId: ModelAttributeId
) : MedatarunException(
    "Attribute '${attributeId.value}' is not declared for entity definition '${entityDefId.value}'"
)
