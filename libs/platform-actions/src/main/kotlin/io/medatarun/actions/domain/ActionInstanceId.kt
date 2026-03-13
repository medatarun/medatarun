package io.medatarun.actions.domain

import io.medatarun.type.commons.id.Id
import java.util.*

/**
 * Unique identifier ov an action call
 */
@JvmInline
value class ActionInstanceId(override val value: UUID): Id<ActionInstanceId>
