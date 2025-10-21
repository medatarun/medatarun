package io.medatarun.model.infra

import io.medatarun.model.model.MedatarunException
import io.medatarun.model.model.ModelId

class ModelRepositoryInMemoryModelNotFoundException(modelId: ModelId):
        MedatarunException("Model not found in repository ${modelId.value}")
