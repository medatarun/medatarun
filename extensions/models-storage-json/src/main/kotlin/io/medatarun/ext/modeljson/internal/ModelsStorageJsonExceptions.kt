package io.medatarun.ext.modeljson.internal

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelId

internal class ModelJsonRepositoryException(message: String) : MedatarunException(message)
internal class ModelJsonRepositoryModelNotFoundException(modelId: ModelId) :
    MedatarunException("Model with id ${modelId.value} not found in Json repository")
internal class ModelJsonEntityIdentifierAttributeNotFound(attributeKey: String) :
        MedatarunException("Error on entity storage. Specified key [$attributeKey] in Json doesn't match any known attribute. Storage needs to be fixed manually.")
internal class ModelJsonEntityAttributeTypeNotFoundException(attributeKey: String, attributeType: String) :
        MedatarunException("Error on entity storage. In Json, key [$attributeKey] declares type [$attributeType] that was not found in Json types. Storage needs to be fixed manually.")
internal class ModelJsonWriterEntityIdentifierAttributeNotFoundInAttributes(entityId: EntityId, attributeId: AttributeId) :
        MedatarunException("Error on entity ${entityId.value} storage while converting to Json. Cannot find ${attributeId.value} in entity's attributes.")
