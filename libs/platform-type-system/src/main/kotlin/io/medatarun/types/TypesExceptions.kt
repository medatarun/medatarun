package io.medatarun.types

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode

class TypeJsonEquivUnknownException(code: String) : MedatarunTechnicalException("Unknown TypeJsonEquiv [$code]")
class TypeJsonConverterIllegalNullException : MedatarunTechnicalException("Unexpected null value in TypeJsonConverter. Should have been handled before.")
open class TypeJsonConverterBadFormatException(msg: String) : MedatarunUserException(msg, StatusCode.BAD_REQUEST)
class TypeJsonJsonObjectExpectedException : TypeJsonConverterBadFormatException("expected a JsonObject")
class TypeJsonJsonStringExpectedException : TypeJsonConverterBadFormatException("expected a JsonString")
