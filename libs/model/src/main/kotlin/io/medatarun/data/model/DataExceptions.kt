package io.medatarun.data.model

import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.MedatarunException
import io.medatarun.model.domain.ModelId

class DataStorageNotFoundException(modelId: ModelId, entityDefId: EntityDefId) :
        MedatarunException("Can not find data repository for model $modelId and entity $entityDefId")