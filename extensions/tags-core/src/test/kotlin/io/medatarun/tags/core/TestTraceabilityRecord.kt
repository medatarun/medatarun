package io.medatarun.tags.core

import io.medatarun.security.AppActorId
import io.medatarun.security.AppTraceabilityRecord
import io.medatarun.type.commons.id.Id

class TestTraceabilityRecord(
    override val actorId: AppActorId = Id.generate(::AppActorId),
    override val origin: String = "tests"
): AppTraceabilityRecord