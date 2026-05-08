package io.medatarun.cli

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode

class CliParameterFormatException(argument: String) :
    MedatarunUserException("Invalid parameter format. Expected --param=value but got [$argument]", StatusCode.BAD_REQUEST)

class CliParameterUnknownException(parameter: String) :
    MedatarunUserException("Unknown parameter [$parameter] for this action", StatusCode.BAD_REQUEST)

class CliParameterMissingException(parameter: String) :
    MedatarunUserException("Missing required parameter [$parameter]", StatusCode.BAD_REQUEST)

class CliParameterBooleanValueException(parameter: String, value: String) :
    MedatarunUserException("Expected boolean value for [$parameter] but got [$value]", StatusCode.BAD_REQUEST)

class CliParameterNumberValueException(parameter: String, value: String) :
    MedatarunUserException("Expected number value for [$parameter] but got [$value]", StatusCode.BAD_REQUEST)

class CliParameterObjectValueException(parameter: String, value: String) :
    MedatarunUserException("Expected JSON object value for [$parameter] but got [$value]", StatusCode.BAD_REQUEST)

class CliParameterArrayValueException(parameter: String, value: String) :
    MedatarunUserException("Expected JSON array value for [$parameter] but got [$value]", StatusCode.BAD_REQUEST)

class CliParameterJsonParseException(parameter: String, expectedType: String, value: String) :
    MedatarunUserException("Invalid JSON value for [$parameter] ($expectedType). Got [$value]", StatusCode.BAD_REQUEST)
