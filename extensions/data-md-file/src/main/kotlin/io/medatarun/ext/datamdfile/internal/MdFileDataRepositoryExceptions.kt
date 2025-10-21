package io.medatarun.ext.datamdfile.internal

import io.medatarun.model.model.MedatarunException
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId

class MdFileEntityDefinitionNotManagedException(
    modelId: ModelId,
    entityDefId: EntityDefId
) : MedatarunException(
    "MdFileDataRepository does not manage entity definition '${entityDefId.value}' for model '${modelId.value}'"
)

class MdFileEntityIdMissingException(
    entityDefId: EntityDefId
) : MedatarunException(
    "Markdown entity for definition '${entityDefId.value}' must define attribute 'id'"
)

class MdFileEntityNotFoundException(
    entityDefId: EntityDefId,
    instanceId: String
) : MedatarunException(
    "Markdown entity '${instanceId}' not found for definition '${entityDefId.value}'"
)

class MdFileEntityAttributeUnknownException(
    entityDefId: EntityDefId,
    attributeId: AttributeDefId
) : MedatarunException(
    "Attribute '${attributeId.value}' is not declared for entity definition '${entityDefId.value}'"
)
