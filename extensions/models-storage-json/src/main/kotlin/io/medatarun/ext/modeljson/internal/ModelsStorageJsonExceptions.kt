package io.medatarun.ext.modeljson.internal

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.ModelId

internal class ModelJsonRepositoryException(message: String) : MedatarunException(message)
internal class ModelJsonRepositoryModelNotFoundException(modelId: ModelId) :
    MedatarunException("Model with id ${modelId.value} not found in Json repository")
