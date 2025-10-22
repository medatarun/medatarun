package io.medatarun.data.model

import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.MedatarunException
import io.medatarun.model.model.ModelId

class DataStorageNotFoundException(modelId: ModelId, entityDefId: EntityDefId) :
        MedatarunException("Can not find data repository for model $modelId and entity $entityDefId")