package io.medatarun.cli

import io.medatarun.lang.exceptions.MedatarunException

class CliParameterFormatException(argument: String) :
    MedatarunException("Invalid parameter format. Expected --param=value but got [$argument]")

class CliParameterUnknownException(parameter: String) :
    MedatarunException("Unknown parameter [$parameter] for this action")

class CliParameterMissingException(parameter: String) :
    MedatarunException("Missing required parameter [$parameter]")

class CliParameterBooleanValueException(parameter: String, value: String) :
    MedatarunException("Expected boolean value for [$parameter] but got [$value]")

class CliParameterNumberValueException(parameter: String, value: String) :
    MedatarunException("Expected number value for [$parameter] but got [$value]")

class CliParameterObjectValueException(parameter: String, value: String) :
    MedatarunException("Expected JSON object value for [$parameter] but got [$value]")

class CliParameterArrayValueException(parameter: String, value: String) :
    MedatarunException("Expected JSON array value for [$parameter] but got [$value]")

class CliParameterJsonParseException(parameter: String, expectedType: String, value: String) :
    MedatarunException("Invalid JSON value for [$parameter] ($expectedType). Got [$value]")
