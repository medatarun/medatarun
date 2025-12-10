package io.medatarun.ext.datamdfile.internal

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.MedatarunException
import io.medatarun.model.domain.ModelKey

class MdFileEntityDefinitionNotManagedException(
    modelKey: ModelKey,
    entityKey: EntityKey
) : MedatarunException(
    "MdFileDataRepository does not manage entity definition '${entityKey.value}' for model '${modelKey.value}'"
)

class MdFileEntityIdMissingException(
    entityKey: EntityKey
) : MedatarunException(
    "Markdown entity for definition '${entityKey.value}' must define attribute 'id'"
)

class MdFileEntityNotFoundException(
    entityKey: EntityKey,
    instanceId: String
) : MedatarunException(
    "Markdown entity '${instanceId}' not found for definition '${entityKey.value}'"
)

class MdFileEntityAttributeUnknownException(
    entityKey: EntityKey,
    attributeId: AttributeKey
) : MedatarunException(
    "Attribute '${attributeId.value}' is not declared for entity definition '${entityKey.value}'"
)
