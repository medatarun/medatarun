package io.medatarun.model.domain

import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class BusinessKeyId(override val value: UUID): Id<BusinessKeyId>
