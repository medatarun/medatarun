package io.medatarun.actions.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import kotlin.reflect.KType

class ActionInvocationException(
    val status: StatusCode,
    message: String,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message, status)

class ActionDefinitionWithoutDocException(group: String, name: String) :
    MedatarunException("All actions must have a documentation annotation. Issue in $group/$name")

class ActionDefinitionWithUnknownSecurityRule(group: String, name: String, rule: String) :
    MedatarunException("Unknown or undefined security rule on action $group/$name: [$rule]")

class UndefinedMultiplatformTypeException(type: KType) : MedatarunException(
    "Type $type has no multiplatform equivalent"
)
class ActionNotFoundInternalException(id: ActionId): MedatarunException(
    "Action $id not found in registry. This is mostly an internal error",
    StatusCode.INTERNAL_SERVER_ERROR
)
class ActionInvokerNotFoundInternalException(id: ActionId): MedatarunException(
    "Action $id has no invoker. This is an internal error",
    StatusCode.INTERNAL_SERVER_ERROR
)
class ActionNotFoundByKeysInternalException(actionGroupKey: String, actionKey: String): MedatarunException(
    "Action $actionGroupKey/$actionKey not found in registry. This is mostly an internal error",
    StatusCode.INTERNAL_SERVER_ERROR
)

class ActionSemanticsInvalidSubjectFormatException(actionKey: String, subject: String) : MedatarunException(
    "Invalid action [$actionKey] semantics subject format [$subject], expected format is type(param,param)"
)

class ActionSemanticsAutoInferenceException(actionKey: String, reason: String) : MedatarunException(
    "Could not infer semantics for action [$actionKey] in AUTO mode: $reason. " +
        "Set explicit semantics declaration on ActionDoc for this action."
)
