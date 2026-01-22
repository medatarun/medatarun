package io.medatarun.ext.modeljson.internal

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.ModelKey

internal class ModelJsonRepositoryException(message: String) : MedatarunException(message)
internal class ModelJsonRepositoryModelNotFoundException(modelKey: ModelKey) :
    MedatarunException("Model with id ${modelKey.value} not found in Json repository")
