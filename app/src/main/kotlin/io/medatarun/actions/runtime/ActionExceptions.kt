package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.model.domain.MedatarunException
import kotlin.reflect.KType


class ActionInvocationException(
    val status: HttpStatusCode,
    message: String,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message)

class ActionDefinitionWithoutDocException(group:String, name: String): MedatarunException("All actions must have a documentation annotation")
class ActionDefinitionWithUnknownSecurityRule(group:String, name: String, rule: String): MedatarunException("Unknown or undefined security rule on action $group,/$name: [$rule]")
class ActionGroupNotFoundException(group: String) : MedatarunException("Unknown action group $group")
class UndefinedMultiplatformTypeException(type: KType) : MedatarunException(
    "Type $type has no multiplatform equivalent"
)
