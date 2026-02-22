package io.medatarun.tags.core.domain

import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class TagId(override val value: UUID) : Id<TagId>
