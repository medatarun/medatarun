package io.medatarun.model.domain

import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class EntityPBKeyId(override val value: UUID): Id<EntityPBKeyId>
