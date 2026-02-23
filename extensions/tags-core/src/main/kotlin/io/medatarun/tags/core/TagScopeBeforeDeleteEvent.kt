package io.medatarun.tags.core

import io.medatarun.platform.kernel.Event
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType

data class TagScopeBeforeDeleteEvent(val tagScopeRef: TagScopeRef): Event
