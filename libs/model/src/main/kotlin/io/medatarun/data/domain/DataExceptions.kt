package io.medatarun.data.domain

import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.MedatarunException
import io.medatarun.model.domain.ModelKey

class DataStorageNotFoundException(modelKey: ModelKey, entityKey: EntityKey) :
        MedatarunException("Can not find data repository for model $modelKey and entity $entityKey")