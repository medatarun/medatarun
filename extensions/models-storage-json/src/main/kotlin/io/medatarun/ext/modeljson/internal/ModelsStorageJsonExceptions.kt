package io.medatarun.ext.modeljson.internal

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.ModelId

internal class ModelJsonRepositoryException(message: String) : MedatarunException(message)
internal class ModelJsonRepositoryModelNotFoundException(modelId: ModelId) :
    MedatarunException("Model with id ${modelId.value} not found in Json repository")
internal class ModelJsonEntityIdentifierAttributeNotFound(entityKey: String) :
        MedatarunException("Error on entity $entityKey storage. Specified key doesn't match any known attribute. Storage needs to be fixed manually.")