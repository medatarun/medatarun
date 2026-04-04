package io.medatarun.auth.domain.actor

import io.medatarun.type.commons.id.Id
import java.util.UUID

data class RoleId(override val value: UUID): Id<RoleId>