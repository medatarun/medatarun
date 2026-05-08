package io.medatarun.actions.domain

import io.medatarun.actions.internal.ActionParamBindingState
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.lang.http.StatusCode
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

abstract class ActionInvocationException(
    val status: StatusCode,
    message: String,
    userVisible: Boolean,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message, status, userVisible)

abstract class ActionInvocationTechnicalException(
    message: String,
    payload: Map<String, String> = emptyMap()
) : ActionInvocationException(
    status = StatusCode.INTERNAL_SERVER_ERROR,
    message = message,
    userVisible = false,
    payload = payload
)

abstract class ActionInvocationUserException(
    status: StatusCode,
    message: String,
    payload: Map<String, String> = emptyMap()
) : ActionInvocationException(
    status= status,
    message = message,
    userVisible = true,
    payload = payload
)

class ActionInvocationActionGroupKeyRequiredException() : ActionInvocationUserException(
    status = StatusCode.BAD_REQUEST,
    message = "Missing actionGroupKey name.",
)

class ActionInvocationActionKeyRequiredException() : ActionInvocationUserException(
    status = StatusCode.BAD_REQUEST,
    message = "Missing actionKey name.",
)

class ActionInvocationNotFoundException(actionGroupKey: String, actionKey: String) : ActionInvocationUserException(
    status = StatusCode.NOT_FOUND,
    message = "Action $actionGroupKey/$actionKey not found.",
)

class ActionInvocationUnauthorizedException(securityRuleEvaluationResultMsg: String) : ActionInvocationUserException(
    status = StatusCode.UNAUTHORIZED,
    message = "Unauthorized",
    payload = mapOf("details" to securityRuleEvaluationResultMsg)
)

class ActionInvocationForbiddenException(securityRuleEvaluationResultMsg: String) : ActionInvocationUserException(
    status = StatusCode.FORBIDDEN,
    message = "Forbidden",
    payload = mapOf("details" to securityRuleEvaluationResultMsg)
)

class ActionInvocationClassHasNoPrimaryConstructorException(actionClass: KClass<out Any>) :
    ActionInvocationTechnicalException(
        "Action class $actionClass has no primary constructor"
    )

class ActionInvocationParameterWithNoNameException(parameter: KParameter) :
    ActionInvocationTechnicalException(
        "Parameter [${parameter}] has no name"
    )

class ActionInvocationParameterDescriptorNotFoundException(paramSerialName: String) :
    ActionInvocationTechnicalException(
        "No action parameter descriptor found for [$paramSerialName]"
    )

class ActionInvocationParameterNotFoundInPayloadException(paramSerialName: String) :
    ActionInvocationTechnicalException(
        "Parameter [${paramSerialName}] could not be found in Json payload"
    )

class ActionInvocationProviderNotFoundException(actionId: ActionId) :
    ActionInvocationTechnicalException(
        "Action provider not found for [$actionId]"
    )


internal class ActionInvocationValidationErrorException(state: ActionParamBindingState.Error) :
    ActionInvocationException(
        status = state.statusCode,
        message = state.message,
        userVisible = true,
        payload = mapOf("details" to state.message)
    )

internal class ActionInvocationValidationMissingException(param: String) : ActionInvocationException(
    status = StatusCode.BAD_REQUEST,
    message = "Parameter [$param] is missing",
    userVisible = true,
    payload = mapOf("details" to "Missing parameter [$param]")
)

internal class ActionInvocationValidationOverflowException(param: KParameter) : ActionInvocationException(
    status = StatusCode.INTERNAL_SERVER_ERROR,
    message = "Parameter [${param.name}] is invalid. Error should have been thrown before.",
    userVisible = true,
    payload = mapOf("details" to "Parameter [${param.name}] is invalid. Error should have been thrown before.")
)

class ActionInvocationUnsupportedTypeException(type: KType) : ActionInvocationTechnicalException(
    message = "Unsupported type $type",
)

class ActionInvocationTypeJsonConverterIllegalNullException(e: TypeJsonConverterIllegalNullException) :
    ActionInvocationTechnicalException(
        message = e.msg,
    )

class ActionInvocationListTypeGenericException() : ActionInvocationTechnicalException(
    message = "List type has no generic argument",
)

class ActionInvocationJsonScalarTypeConvertException(type: KType) : ActionInvocationTechnicalException(
    message = "Can not manage KType of kind [$type]",
)

class ActionInvocationJsonScalarTypeUnsupportedException(kclass: KClass<*>) : ActionInvocationTechnicalException(
    message = "Unsupported parameter type: ${kclass.simpleName}",
)

class ActionInvocationJsonMapNoGenericKeyException() : ActionInvocationTechnicalException(
    message = "Map type has no key generic argument"
)

class ActionInvocationJsonMapNoGenericValueException() : ActionInvocationTechnicalException(
    message = "Map type has no key generic argument"
)

class ActionInvocationValueClassNoConstructorException(kclass: KClass<*>) : ActionInvocationTechnicalException(
    message = "No constructor for value class ${kclass.simpleName}"
)

class ActionInvocationDataClassNoPrimaryConstructorException(kclass: KClass<*>) : ActionInvocationTechnicalException(
    message = "No primary constructor for data class ${kclass.simpleName}"
)

class ActionInvocationDeserializePayloadRawMismatchException(
    actionPayloadClass: KClass<*>,
    actionGroupKey: String,
    actionKey: String
) :
    ActionInvocationTechnicalException(
        "Action payload class ${actionPayloadClass} does't match expected payload for ${actionGroupKey}/${actionKey}."
    )

class ActionInvocationClassFromDescriptorNotFoundException(
    actionGroupKey: String,
    actionKey: String,
    actionClassName: String
) :
    ActionInvocationTechnicalException(
        "Action ${actionGroupKey}/${actionKey} not found by descriptor class $actionClassName"
    )

class ActionDefinitionWithoutDocException(group: String, name: String) :
    MedatarunTechnicalException("All actions must have a documentation annotation. Issue in $group/$name")

class ActionDefinitionWithUnknownSecurityRule(group: String, name: String, rule: String) :
    MedatarunTechnicalException("Unknown or undefined security rule on action $group/$name: [$rule]")

class UndefinedMultiplatformTypeException(type: KType) :
    MedatarunTechnicalException("Type $type has no multiplatform equivalent")

class ActionNotFoundInternalException(id: ActionId) :
    MedatarunTechnicalException(
        "Action $id not found in registry. This is mostly an internal error",
        StatusCode.INTERNAL_SERVER_ERROR
    )

class ActionInvokerNotFoundInternalException(id: ActionId) :
    MedatarunTechnicalException(
        "Action $id has no invoker. This is an internal error",
        StatusCode.INTERNAL_SERVER_ERROR
    )

class ActionNotFoundByKeysInternalException(actionGroupKey: String, actionKey: String) :
    MedatarunTechnicalException(
        "Action $actionGroupKey/$actionKey not found in registry. This is mostly an internal error",
        StatusCode.INTERNAL_SERVER_ERROR
    )

class ActionSemanticsInvalidSubjectFormatException(actionKey: String, subject: String) :
    MedatarunTechnicalException(
        "Invalid action [$actionKey] semantics subject format [$subject], expected format is type(param,param)"
    )

class ActionSemanticsAutoInferenceException(actionKey: String, reason: String) :
    MedatarunTechnicalException(
        "Could not infer semantics for action [$actionKey] in AUTO mode: $reason. " +
                "Set explicit semantics declaration on ActionDoc for this action."
    )
