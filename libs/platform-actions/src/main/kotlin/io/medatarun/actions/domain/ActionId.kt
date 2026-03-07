package io.medatarun.actions.domain

import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class ActionId(override val value: UUID) : Id<ActionId>