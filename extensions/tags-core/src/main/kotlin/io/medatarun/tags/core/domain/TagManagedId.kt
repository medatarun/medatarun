package io.medatarun.tags.core.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class TagManagedId(override val value: UUID) : Id<TagManagedId>