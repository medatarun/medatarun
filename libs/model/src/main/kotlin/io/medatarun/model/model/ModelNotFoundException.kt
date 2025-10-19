package io.medatarun.model.model

import io.medatarun.model.model.ModelId

open class MedatarunException(message: String) : Exception(message)
class ModelNotFoundException(id: ModelId): MedatarunException("Model with id $id was not found")
