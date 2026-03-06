package io.medatarun.actions.runtime

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import kotlin.reflect.KType


class ActionInvocationException(
    val status: StatusCode,
    message: String,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message, status)

class ActionDefinitionWithoutDocException(group:String, name: String): MedatarunException("All actions must have a documentation annotation. Issue in $group/$name")
class ActionDefinitionWithUnknownSecurityRule(group:String, name: String, rule: String): MedatarunException("Unknown or undefined security rule on action $group,/$name: [$rule]")

class UndefinedMultiplatformTypeException(type: KType) : MedatarunException(
    "Type $type has no multiplatform equivalent"
)
