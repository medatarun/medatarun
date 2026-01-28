package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.EntityId

class ModelInMemoryEntityIdentifierPointsToUnknownAttributeException(entityId: EntityId) :
        MedatarunException("Corrupted Entity ${entityId.value}. Identifier attribute points to an unknown attribute.")