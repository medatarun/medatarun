package io.medatarun.tags.core.domain

import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class TagGroupId(override val value: UUID) : Id<TagGroupId>
