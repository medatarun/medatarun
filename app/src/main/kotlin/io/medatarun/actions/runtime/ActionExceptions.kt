package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.model.domain.MedatarunException


class ActionInvocationException(
    val status: HttpStatusCode,
    message: String,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message)

class ActionGroupNotFoundException(group: String) : MedatarunException("Unknown action group $group")
