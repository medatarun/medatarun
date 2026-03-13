package io.medatarun.actions.domain

import io.medatarun.type.commons.id.Id
import java.util.*

/**
 * Unique identifier of an action in the action registry.
 */
@JvmInline
value class ActionId(override val value: UUID) : Id<ActionId>